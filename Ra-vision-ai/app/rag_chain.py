import os
import re
import json
from langchain_huggingface import HuggingFaceEndpoint, ChatHuggingFace
from langchain_core.prompts import ChatPromptTemplate, SystemMessagePromptTemplate, HumanMessagePromptTemplate
from langchain_core.output_parsers import StrOutputParser

from .database import (
    buscar_funcionario_por_matricula,
    buscar_total_vendas_funcionario,
    buscar_vendas_loja,
    buscar_comissao_calculada,
    buscar_jornada_comissao,
    buscar_percentual_comissao,
    buscar_gerentes_loja
)
from .intent_parser import parse_intent
from .prompts import SYSTEM_PROMPT, build_context_prompt
from .tools import criar_regra_negocio_dinamica, atualizar_regra_negocio_dinamica, buscar_regra_por_id, buscar_jornada_comissao as tool_buscar_jornada

# ─────────────────────────────────────────────────────────────────
# System Prompt rigoroso para o fluxo de Criação de Regras
# ─────────────────────────────────────────────────────────────────
RULE_CREATION_SYSTEM_PROMPT = """Você é o **Assistente de Configuração de Regras** do sistema Ra Vision.

## Sua Missão
Guiar o Gestor de RH ou Administrador na criação de regras de negócio dinâmicas de comissão.
Você NUNCA deve inventar dados ou acionar a ferramenta sem ter TODOS os parâmetros.

## Tipos de Regras Disponíveis
Quando o usuário disser que quer criar uma regra, apresente estas 5 opções:

1. **BONUS_FIXO** — Bônus Fixo Nominal: soma um valor em Reais diretamente na comissão final do funcionário.
   Exemplo: "Dar R$ 500 de bônus para MATRIC-134."
2. **BONUS_BASE** — Bônus na Base de Cálculo: soma um valor na base de vendas ANTES de aplicar o percentual.
   Exemplo: "Somar R$ 20.000 na base de vendas de todos os cargos 200."
3. **OVERRIDE_PERCENTUAL** — Override de Percentual: substitui completamente a taxa de comissão do funcionário.
   Exemplo: "Aplicar 8.5% de comissão para a marca 10."
4. **BLACK_FRIDAY** — Black Friday: acréscimo percentual aplicado proporcionalmente aos 7 últimos dias de Novembro.
   Exemplo: "+1% para vendedores em Nov/2025" (valor = 1.0).
5. **FAIXA_VENDAS** — Faixa de Vendas: bônus escalonado por performance de vendas mínima.
   Exemplo: "Se o vendedor vender mais de R$ 50.000, dar R$ 4.000 de bônus."

## Regras de Comportamento (OBRIGATÓRIO)

### NUNCA crie uma regra sem ter TODOS estes 4 parâmetros confirmados:
1. **Tipo da Regra** — qual dos 5 tipos acima.
2. **Mês de Competência** — no formato YYYY-MM (ex: "2025-11").
3. **Valor Modificador** — o número (em Reais, percentual, ou meta mínima, dependendo do tipo).
4. **Condição de Aplicação** — para QUEM se aplica: uma matrícula específica (ex: "MATRIC-134"), um código de cargo (ex: 200), um código de marca (ex: 10), ou vazio {} se for para todos.

### Se faltar qualquer parâmetro:
- NÃO chame a ferramenta.
- Pergunte educadamente ao usuário o que falta.
- Exemplo: "Entendi que você quer dar um bônus de R$ 500. Para qual matrícula devo aplicar? E em qual mês de competência?"

### Se o usuário fornecer todos os parâmetros:
- Confirme os dados com o usuário antes de chamar a ferramenta.
- Exemplo: "Perfeito! Vou criar a seguinte regra: **Bônus Fixo** de **R$ 500** para **MATRIC-134** em **Agosto/2025**. Confirma?"
- Se o usuário confirmar, aí sim chame a ferramenta `criar_regra_negocio_dinamica`.

### Formato do campo `condicoes`:
O campo `condicoes` deve ser um JSON válido em string. Exemplos:
- Para matrícula: '{{"matricula": "MATRIC-134"}}'
- Para cargo: '{{"codCargo": 200}}'
- Para marca: '{{"codMarca": 10}}'
- Para faixa de vendas: '{{"minVendas": 50000}}'
- Para todos: '{{}}'

## Formato da Resposta
Sempre responda em **português brasileiro**, com formatação amigável em **Markdown**.
"""


# ─────────────────────────────────────────────────────────────────
# Builder do Contexto — Jornada Completa da Comissão (RAV-44)
# ─────────────────────────────────────────────────────────────────

def build_jornada_context_prompt(
    dados_rh: list[dict],
    vendas_individuais: float,
    vendas_loja: float,
    jornada: dict,
    percentual_info: dict | None,
    pergunta: str
) -> str:
    """
    Monta o prompt de contexto com a jornada completa de 3 etapas da comissão.
    Substitui build_context_prompt para o fluxo RAG principal.
    """
    if not dados_rh:
        return f"""## Contexto de Dados:
**ATENÇÃO: Nenhum funcionário encontrado com os critérios informados.**
Não há dados no banco de dados para a matrícula ou competência solicitada.

## Pergunta do Analista:
{pergunta}
"""

    rh = dados_rh[0]
    cod_cargo = rh.get("cod_cargo", 0)
    is_gerente = cod_cargo == 150
    tipo_cargo = "Gerente" if is_gerente else "Vendedor"
    base_vendas = vendas_loja if is_gerente else vendas_individuais

    # Percentual de comissão
    pct_text = "Não encontrado na tabela de comissionamento."
    if percentual_info:
        pct_text = f"{percentual_info.get('pct_comiss', 0)}% ({percentual_info.get('descri_cargo', '')} — {percentual_info.get('descr_marca', '')})"

    # Dados de admissão/demissão
    data_admiss = rh.get("data_admiss", "N/A")
    data_demiss = rh.get("data_demiss", None)
    demiss_text = str(data_demiss) if data_demiss else "Ativo (sem demissão)"

    # ── Etapa 1: Base ──────────────────────────────────────────────
    etapa_base = jornada.get("etapa_base") or {}
    base_text = "Não processada ainda."
    if etapa_base:
        base_text = f"""
| Campo | Valor |
|-------|-------|
| Total de Vendas | R$ {etapa_base.get('valor_total_vendas', 0):,.2f} |
| Percentual de Comissão | {etapa_base.get('percentual_comissao', 0)}% |
| Comissão Base Bruta | **R$ {etapa_base.get('valor_comissao_bruta', 0):,.2f}** |
"""

    # ── Etapa 2: Proporcional ──────────────────────────────────────
    etapa_prop = jornada.get("etapa_proporcional")
    prop_text = "⏳ **Ainda não processada.** O motor Java ainda executará os ajustes de intercorrências trabalhistas."
    if etapa_prop:
        dias_trab  = etapa_prop.get("dias_trabalhados", "N/A")
        dias_mes   = etapa_prop.get("dias_do_mes", "N/A")
        dias_inter = etapa_prop.get("dias_intercorrencia", 0) or 0
        motivo     = etapa_prop.get("motivo_proporcionalidade") or "Sem intercorrencia registrada"
        valor_ajust = etapa_prop.get("valor_comissao_ajustada", 0)
        prop_text = f"""
| Campo | Valor |
|-------|-------|
| Dias do Mes | {dias_mes} |
| Dias Trabalhados | {dias_trab} |
| Dias de Intercorrencia | {dias_inter} |
| Motivo | {motivo} |
| Valor Apos Ajuste Trabalhista | **R$ {valor_ajust:,.2f}** |
"""

    # ── Etapa 3: Final ─────────────────────────────────────────────
    etapa_final = jornada.get("etapa_final")
    final_text = "⏳ **Ainda não processada.** As regras sazonais e campanhas de RH aprovadas ainda serão aplicadas pelo motor."
    if etapa_final:
        historico  = etapa_final.get("historico_regras_aplicadas") or "Nenhuma regra sazonal aplicada"
        valor_def  = etapa_final.get("valor_comissao_definitiva", 0)
        final_text = f"""
| Campo | Valor |
|-------|-------|
| Historico de Regras Aplicadas | {historico} |
| **Comissao Final Definitiva** | **R$ {valor_def:,.2f}** |
"""

    status = jornada.get("status_processamento", "NAO_ENCONTRADO")
    status_labels = {
        "COMPLETO": "✅ Processamento COMPLETO — todas as 3 etapas executadas.",
        "PARCIAL_PROPORCIONAL": "⚠️ Processamento PARCIAL — Etapas 1 e 2 concluídas. Etapa 3 (bônus sazonais) ainda pendente.",
        "APENAS_BASE": "🔄 Processamento INICIAL — apenas Etapa 1 (base) executada. Etapas 2 e 3 ainda pendentes.",
        "NAO_ENCONTRADO": "❌ Funcionário não encontrado nas tabelas de cálculo para esta competência.",
    }
    status_text = status_labels.get(status, status)

    context = f"""## Contexto de Dados — Jornada de Comissionamento:

**Status do Processamento:** {status_text}

### 👤 Dados do Funcionário (Base RH):
| Campo | Valor |
|-------|-------|
| Matrícula | {rh.get('matricula', 'N/A')} |
| Cargo | {rh.get('descr_cargo', 'N/A')} (Código: {cod_cargo}) |
| Tipo | **{tipo_cargo}** |
| Loja | {rh.get('descr_loja', 'N/A')} (Código: {rh.get('cod_loja', 'N/A')}) |
| Marca | {rh.get('descr_marca', 'N/A')} (Código: {rh.get('cod_marca', 'N/A')}) |
| Data Admissão | {data_admiss} |
| Data Demissão | {demiss_text} |
| Competência (Mês Ref) | {rh.get('date_ref', 'N/A')} |

### 📊 Dados de Vendas:
| Tipo | Valor |
|------|-------|
| Vendas Individuais do Funcionário | R$ {vendas_individuais:,.2f} |
| Vendas Totais da Loja | R$ {vendas_loja:,.2f} |
| **Base de Cálculo Utilizada** | **R$ {base_vendas:,.2f}** ({"Total da Loja — cargo de Gerente" if is_gerente else "Vendas Individuais — cargo de Vendedor"}) |

### 💹 Percentual de Comissão (Tabela de Comissionamento):
{pct_text}

---

### 📌 ETAPA 1 — Comissão Base (Motor Java: tb_comissao_calculada_base):
{base_text}

### ⚖️ ETAPA 2 — Ajuste Proporcional / Intercorrências Trabalhistas (tb_comissao_calculada_proporcional):
{prop_text}

### 🎁 ETAPA 3 — Bônus Sazonais / Campanhas de RH (tb_comissao_calculada_final):
{final_text}

---

## Pergunta do Analista de Operações:
{pergunta}

## Instrução:
Responda a pergunta acima usando EXCLUSIVAMENTE os dados do contexto fornecido.
Siga OBRIGATORIAMENTE os 4 passos numerados da Jornada Matemática da Comissão.
Se alguma etapa ainda não foi processada, deixe isso claro ao usuário.
"""
    return context


def get_rag_chain():
    """
    Configura a chain do LangChain com HuggingFace.
    """
    llm = HuggingFaceEndpoint(
        repo_id="meta-llama/Llama-3.3-70B-Instruct",
        task="text-generation",
        max_new_tokens=1024,
        do_sample=False,
        temperature=0.1,
        huggingfacehub_api_token=os.getenv("HUGGINGFACEHUB_API_TOKEN")
    )
    
    chat_model = ChatHuggingFace(llm=llm)
    
    prompt = ChatPromptTemplate.from_messages([
        SystemMessagePromptTemplate.from_template(SYSTEM_PROMPT),
        HumanMessagePromptTemplate.from_template("{context}")
    ])
    
    chain = prompt | chat_model | StrOutputParser()
    return chain


def _extrair_parametros_regra(message: str, date_ref: str) -> dict:
    """
    Tenta extrair os parâmetros de criação de regra diretamente da mensagem.
    Retorna um dict com os campos encontrados e None nos faltantes.
    """
    msg_lower = message.lower()

    params = {
        "tipo": None,
        "competencia": None,
        "valor": None,
        "condicoes": None,
        "descricao": None,
    }

    # ── 1. Detectar TIPO ────────────────────────────────────────────────────────
    tipo_map = {
        "BONUS_FIXO": [
            "bônus fixo", "bonus fixo", "bônus nominal", "bonus nominal",
        ],
        "BONUS_BASE": [
            "bônus na base", "bonus na base", "bônus base", "bonus base",
            "somar na base", "somar", "na base de cálculo", "na base de calculo",
            "adicionar na base", "base de cálculo", "base de calculo",
            "somar ao base", "incrementar base",
        ],
        "OVERRIDE_PERCENTUAL": [
            "override", "substituição", "substituicao",
            "alterar percentual", "alterar o percentual", "alterar a taxa",
            "mudar percentual", "mudar o percentual", "mudar a taxa",
            "mudar comissão", "mudar comissao", "alterar comissão", "alterar comissao",
            "definir percentual", "configurar percentual", "definir taxa",
            "aplicar percentual", "trocar percentual", "novo percentual",
        ],
        "BLACK_FRIDAY": [
            "black friday", "blackfriday",
        ],
        "FAIXA_VENDAS": [
            "faixa de vendas", "faixa vendas", "escalonado", "performance",
            "se vender acima", "se atingir", "vender mais de", "meta de vendas",
            "acima de", "maior que",
        ],
    }
    for tipo_key, keywords in tipo_map.items():
        if any(kw in msg_lower for kw in keywords):
            params["tipo"] = tipo_key
            break

    # ── 2. Detectar VALOR ───────────────────────────────────────────────────────

    # Caso especial: FAIXA_VENDAS tem DOIS valores — "acima de R$ X → bônus R$ Y"
    # O valor da regra = bônus (segundo R$ ou maior R$)
    if params["tipo"] == "FAIXA_VENDAS":
        todos_valores = re.findall(r'R\$\s*([\d.,]+)', message)
        if len(todos_valores) >= 2:
            # Primeiro = meta mínima (minVendas), Último = bônus (valor da regra)
            def _parse_brl(s):
                return float(s.replace(".", "").replace(",", "."))
            min_vendas_raw = todos_valores[0]
            bonus_raw      = todos_valores[-1]
            params["valor"] = _parse_brl(bonus_raw)
            # minVendas vai para as condições (será mesclado com cargo/marca abaixo)
            params["_min_vendas"] = _parse_brl(min_vendas_raw)
        elif len(todos_valores) == 1:
            params["valor"] = float(todos_valores[0].replace(".", "").replace(",", "."))
        # Fallback: tentar extrair valor percentual ou numérico
        if params["valor"] is None:
            pct_match = re.search(r'(\d+[.,]?\d*)\s*%', message)
            if pct_match:
                params["valor"] = float(pct_match.group(1).replace(",", "."))

    # Caso especial: OVERRIDE_PERCENTUAL — o valor é o novo percentual (X%)
    elif params["tipo"] == "OVERRIDE_PERCENTUAL":
        pct_match = re.search(r'(\d+[.,]?\d*)\s*%', message)
        if pct_match:
            params["valor"] = float(pct_match.group(1).replace(",", "."))
        # Fallback R$
        if params["valor"] is None:
            r_match = re.search(r'R\$\s*([\d.,]+)', message)
            if r_match:
                params["valor"] = float(r_match.group(1).replace(".", "").replace(",", "."))

    # Caso especial: BLACK_FRIDAY — o valor é o acréscimo percentual (+X%)
    elif params["tipo"] == "BLACK_FRIDAY":
        pct_match = re.search(r'[+\-]?\s*(\d+[.,]?\d*)\s*%', message)
        if pct_match:
            params["valor"] = float(pct_match.group(1).replace(",", "."))
        if params["valor"] is None:
            r_match = re.search(r'R\$\s*([\d.,]+)', message)
            if r_match:
                params["valor"] = float(r_match.group(1).replace(".", "").replace(",", "."))

    # Casos gerais: BONUS_FIXO, BONUS_BASE — valor em R$
    else:
        valor_match = re.search(r'R\$\s*([\d.,]+)', message)
        if not valor_match:
            valor_match = re.search(r'(\d+[.,]?\d*)\s*(?:reais|real)', msg_lower)
        if not valor_match:
            valor_match = re.search(
                r'(?:valor|bônus|bonus|de)\s+(?:de\s+)?(?:R\$\s*)?([\d.,]+)',
                message, re.IGNORECASE
            )
        if valor_match:
            try:
                params["valor"] = float(
                    valor_match.group(1).replace(".", "").replace(",", ".")
                )
            except ValueError:
                pass

    # ── 3. Detectar CONDIÇÃO DE APLICAÇÃO ──────────────────────────────────────
    matric_match = re.search(r'MATRIC[- ]?(\d+)', message, re.IGNORECASE)
    cargo_match  = re.search(r'cargo\s+(\d+)', msg_lower)
    marca_match  = re.search(r'marca\s+(\d+)', msg_lower)

    # "para todos", "geral", "funcionários todos", sem alvo específico → {}
    tem_todos = any(w in msg_lower for w in [
        'para todos', 'todos os funcionários', 'todos os funcionarios',
        'funcionários todos', 'geral', 'todos',
    ])

    if matric_match:
        params["condicoes"] = json.dumps(
            {"matricula": f"MATRIC-{int(matric_match.group(1))}"}
        )
    elif cargo_match:
        cond = {"codCargo": int(cargo_match.group(1))}
        # Para FAIXA_VENDAS: mesclar cargo com minVendas
        if params["tipo"] == "FAIXA_VENDAS" and "_min_vendas" in params:
            cond["minVendas"] = params["_min_vendas"]
        params["condicoes"] = json.dumps(cond)
    elif marca_match:
        cond = {"codMarca": int(marca_match.group(1))}
        if params["tipo"] == "FAIXA_VENDAS" and "_min_vendas" in params:
            cond["minVendas"] = params["_min_vendas"]
        params["condicoes"] = json.dumps(cond)
    elif params["tipo"] == "FAIXA_VENDAS" and "_min_vendas" in params:
        # Sem cargo/marca explícito: condição é só a meta mínima
        params["condicoes"] = json.dumps({"minVendas": params["_min_vendas"]})
    elif tem_todos:
        # Sem alvo específico mas com "para todos" → aplica a todos
        params["condicoes"] = json.dumps({})

    # Limpar chave auxiliar que não pertence ao schema final
    params.pop("_min_vendas", None)

    # ── 4. Detectar COMPETÊNCIA (mês) ──────────────────────────────────────────
    from .intent_parser import MESES_MAP
    for mes_nome, mes_num in MESES_MAP.items():
        if mes_nome in msg_lower:
            ano_match = re.search(r'20\d{2}', message)
            ano = ano_match.group(0) if ano_match else "2025"
            params["competencia"] = f"{ano}-{mes_num}"
            break

    return params


async def process_user_query(message: str, date_ref: str, auth_header: str = None):
    """
    Pipeline RAG completo com logs detalhados para debug.
    """
    try:
        # 1. Parse de intenção
        intent = parse_intent(message, date_ref)
        target_date = intent.date_ref or date_ref
        
        print(f"DEBUG: Processando query. Msg: {message}, Data Alvo: {target_date}, Tipo: {intent.tipo_consulta}")
        
        # ─────────────────────────────────────────────────────────
        # Fluxo de Criação de Regra (com validação estrita)
        # ─────────────────────────────────────────────────────────
        if intent.tipo_consulta == "criar_regra":
            print("DEBUG: Iniciando fluxo de criação de regra...")
            try:
                # Setar o token de auth no módulo tools
                import app.tools as tools_module
                tools_module.current_auth_token = auth_header
                
                # Extrair parâmetros da mensagem
                params = _extrair_parametros_regra(message, target_date)
                print(f"DEBUG: Parâmetros extraídos: {params}")
                
                # Verificar quais parâmetros estão faltando
                faltando = []
                if not params["tipo"]:
                    faltando.append("o **tipo da regra** (Bônus Fixo, Bônus Base, Override Percentual, Black Friday ou Faixa de Vendas)")
                if not params["competencia"]:
                    faltando.append("o **mês de competência** (ex: Agosto de 2025)")
                if params["valor"] is None:
                    faltando.append("o **valor modificador** (ex: R$ 500,00)")
                if not params["condicoes"]:
                    faltando.append("a **condição de aplicação** (para qual matrícula, cargo ou marca a regra se aplica)")
                
                # Se faltam parâmetros, pedir ao usuário
                if faltando:
                    lista_faltando = "\n".join([f"- {item}" for item in faltando])
                    
                    resposta = f"Entendi que você quer criar uma regra! 🎯\n\n"
                    
                    # Se nenhum tipo foi detectado, mostrar as opções
                    if not params["tipo"]:
                        resposta += "Nosso sistema suporta **5 tipos de regras**:\n\n"
                        resposta += "1. 🪄 **Bônus Fixo** — Soma um valor direto na comissão final\n"
                        resposta += "2. 📊 **Bônus na Base** — Soma um valor na base de vendas antes do cálculo\n"
                        resposta += "3. 🔄 **Override Percentual** — Substitui a taxa de comissão\n"
                        resposta += "4. 🛍️ **Black Friday** — Acréscimo % nos últimos 7 dias de Novembro\n"
                        resposta += "5. 🎯 **Faixa de Vendas** — Bônus escalonado por performance\n\n"
                    
                    resposta += f"Para eu criar a regra, ainda preciso das seguintes informações:\n\n{lista_faltando}\n\n"
                    resposta += "Me informe esses dados e eu crio a regra para você! 😊"
                    return resposta
                
                # Todos os parâmetros coletados — Montar descrição
                tipo_labels = {
                    "BONUS_FIXO": "Bônus Fixo",
                    "BONUS_BASE": "Bônus na Base",
                    "OVERRIDE_PERCENTUAL": "Override Percentual",
                    "BLACK_FRIDAY": "Black Friday",
                    "FAIXA_VENDAS": "Faixa de Vendas",
                }
                tipo_label = tipo_labels.get(params["tipo"], params["tipo"])
                descricao = f"{tipo_label} - Valor: {params['valor']} - Competência: {params['competencia']}"
                
                # Chamar a tool diretamente (sem Tool Calling do LLM)
                print(f"DEBUG: Criando regra com parâmetros completos: {params}")
                tool_result = criar_regra_negocio_dinamica.invoke({
                    "descricao": descricao,
                    "tipo": params["tipo"],
                    "competencia": params["competencia"],
                    "condicoes": params["condicoes"],
                    "valor": params["valor"],
                })
                
                print(f"DEBUG: Resultado da tool: {tool_result}")
                
                # Formatar resposta bonita
                condicoes_display = params["condicoes"]
                try:
                    cond_obj = json.loads(params["condicoes"])
                    if "matricula" in cond_obj:
                        condicoes_display = f"Matrícula **{cond_obj['matricula']}**"
                    elif "codCargo" in cond_obj:
                        condicoes_display = f"Cargo **{cond_obj['codCargo']}**"
                    elif "codMarca" in cond_obj:
                        condicoes_display = f"Marca **{cond_obj['codMarca']}**"
                    else:
                        condicoes_display = "**Todos os funcionários**"
                except Exception:
                    pass
                
                resposta = f"✅ **Regra criada com sucesso!**\n\n"
                resposta += f"| Campo | Valor |\n|-------|-------|\n"
                resposta += f"| Tipo | {tipo_label} |\n"
                resposta += f"| Competência | {params['competencia']} |\n"
                resposta += f"| Valor | {params['valor']} |\n"
                resposta += f"| Aplicação | {condicoes_display} |\n\n"
                resposta += f"📋 {tool_result}\n\n"
                resposta += "> A regra foi salva com status **PENDENTE** e aguarda aprovação do Gestor de RH na tela de Regras."
                return resposta
                
            except Exception as tool_err:
                print(f"ERROR no fluxo de criação de regra: {tool_err}")
                return f"❌ Houve um erro ao tentar criar a regra: {tool_err}"

        # ─────────────────────────────────────────────────────────
        # Fluxo de Edição de Regra
        # ─────────────────────────────────────────────────────────
        elif intent.tipo_consulta == "editar_regra":
            print("DEBUG: Iniciando fluxo de edição de regra...")
            try:
                import app.tools as tools_module
                tools_module.current_auth_token = auth_header

                # 1. Extrair o ID da regra da mensagem
                id_match = re.search(
                    r'(?:regra\s+(?:de\s+)?(?:id\s+)?|\bid\s+|#)(\d+)',
                    message, re.IGNORECASE
                )
                if not id_match:
                    return (
                        "Para **atualizar** uma regra, preciso saber o **ID** dela. ✏️\n\n"
                        "Você pode verificar o ID na tela de **Gerenciar Regras**.\n\n"
                        "**Exemplo de uso:**\n"
                        "> *'Altera a regra de ID 5 para R$ 1.250,50 e altere a competência para Dezembro de 2025'*"
                    )

                regra_id = int(id_match.group(1))
                print(f"DEBUG: ID da regra a editar: {regra_id}")

                # 2. Buscar dados atuais da regra no backend
                regra_atual = buscar_regra_por_id(regra_id)
                if not regra_atual:
                    return (
                        f"❌ Não encontrei nenhuma regra com **ID {regra_id}**.\n\n"
                        "Verifique o ID na tela de Gerenciar Regras e tente novamente."
                    )
                print(f"DEBUG: Regra atual encontrada: {regra_atual}")

                # 3. Extrair campos que o usuário quer alterar (mesmo parser da criação)
                novos = _extrair_parametros_regra(message, target_date)
                print(f"DEBUG: Novos parâmetros extraídos: {novos}")

                # 4. Merge: usa o novo valor se informado, senão mantém o atual
                tipo_final      = novos["tipo"]       or regra_atual.get("tipoRegra")
                competencia_final = novos["competencia"] or regra_atual.get("mesCompetencia")
                valor_final     = novos["valor"]      if novos["valor"] is not None else float(regra_atual.get("valorModificador", 0))
                condicoes_final = novos["condicoes"]  or regra_atual.get("condicoesAplicacao") or "{}"

                tipo_labels = {
                    "BONUS_FIXO": "Bônus Fixo", "BONUS_BASE": "Bônus na Base",
                    "OVERRIDE_PERCENTUAL": "Override Percentual",
                    "BLACK_FRIDAY": "Black Friday", "FAIXA_VENDAS": "Faixa de Vendas",
                }
                tipo_label = tipo_labels.get(tipo_final, tipo_final)
                descricao_final = f"{tipo_label} - Valor: {valor_final} - Competência: {competencia_final} (editado via chat)"

                # 5. Chamar a tool de atualização
                print(f"DEBUG: Atualizando regra ID {regra_id} com: tipo={tipo_final}, valor={valor_final}, competencia={competencia_final}")
                tool_result = atualizar_regra_negocio_dinamica.invoke({
                    "regra_id": regra_id,
                    "descricao": descricao_final,
                    "tipo": tipo_final,
                    "competencia": competencia_final,
                    "condicoes": condicoes_final,
                    "valor": valor_final,
                })
                print(f"DEBUG: Resultado da atualização: {tool_result}")

                # 6. Formatar exibição das condições
                def formatar_condicoes(cond_json: str) -> str:
                    try:
                        cond = json.loads(cond_json)
                        if "matricula" in cond:  return f"Matrícula **{cond['matricula']}**"
                        if "codCargo" in cond:   return f"Cargo **{cond['codCargo']}**"
                        if "codMarca" in cond:   return f"Marca **{cond['codMarca']}**"
                        if "minVendas" in cond:  return f"Meta mín. **R$ {cond['minVendas']}**"
                        return "**Todos os funcionários**"
                    except Exception:
                        return cond_json

                cond_antes  = formatar_condicoes(regra_atual.get("condicoesAplicacao") or "{}")
                cond_depois = formatar_condicoes(condicoes_final)

                resposta  = f"✅ **Regra ID {regra_id} atualizada com sucesso!**\n\n"
                resposta += f"| Campo | Antes | Depois |\n|-------|-------|--------|\n"
                resposta += f"| Tipo | {regra_atual.get('tipoRegra')} | {tipo_final} |\n"
                resposta += f"| Competência | {regra_atual.get('mesCompetencia')} | {competencia_final} |\n"
                resposta += f"| Valor | {regra_atual.get('valorModificador')} | {valor_final} |\n"
                resposta += f"| Aplicação | {cond_antes} | {cond_depois} |\n\n"
                resposta += f"📋 {tool_result}\n\n"
                resposta += "> A regra voltou para status **PENDENTE** e aguarda nova aprovação do Gestor de RH."
                return resposta

            except Exception as edit_err:
                print(f"ERROR no fluxo de edição de regra: {edit_err}")
                return f"❌ Houve um erro ao tentar atualizar a regra: {edit_err}"

        # ─────────────────────────────────────────────────────────
        # Fluxo RAG padrão (consultas de comissão, detalhamento, etc)
        # ─────────────────────────────────────────────────────────
        
        # 2. Retrieval
        context_data = {
            "dados_rh": [],
            "vendas_individuais": 0.0,
            "vendas_loja": 0.0,
            "comissao_calculada": [],
            "jornada_comissao": {"encontrado": False, "status_processamento": "NAO_ENCONTRADO"},
            "percentual_info": None
        }
        
        # Caso A: Busca por Matrícula
        if intent.matricula:
            print(f"DEBUG: Buscando por matrícula {intent.matricula}...")
            context_data["dados_rh"] = buscar_funcionario_por_matricula(intent.matricula, target_date)
        
        # Caso B: Busca por Gerente de Loja
        elif intent.cod_loja and intent.tipo_consulta == "gerente":
            print(f"DEBUG: Buscando gerente da loja {intent.cod_loja}...")
            context_data["dados_rh"] = buscar_gerentes_loja(intent.cod_loja, target_date)

        # Enriquecimento de dados
        if context_data["dados_rh"]:
            rh_principal = context_data["dados_rh"][0]
            matricula_alvo = rh_principal.get("matricula")
            cod_loja_alvo = rh_principal.get("cod_loja")

            context_data["vendas_individuais"] = buscar_total_vendas_funcionario(matricula_alvo, target_date)
            context_data["vendas_loja"] = buscar_vendas_loja(cod_loja_alvo, target_date)
            context_data["comissao_calculada"] = buscar_comissao_calculada(matricula_alvo, target_date)
            context_data["jornada_comissao"] = buscar_jornada_comissao(matricula_alvo, target_date)
            context_data["percentual_info"] = buscar_percentual_comissao(
                rh_principal.get("cod_marca"),
                rh_principal.get("cod_cargo")
            )
            print(f"DEBUG: Enriquecimento concluído para {matricula_alvo}. Status jornada: {context_data['jornada_comissao'].get('status_processamento')}")
        else:
            print(f"DEBUG: Nenhum dado de RH encontrado para os critérios.")
                
        # 3. Build Context Prompt
        context_prompt = build_jornada_context_prompt(
            dados_rh=context_data["dados_rh"],
            vendas_individuais=context_data["vendas_individuais"],
            vendas_loja=context_data["vendas_loja"],
            jornada=context_data["jornada_comissao"],
            percentual_info=context_data["percentual_info"],
            pergunta=message
        )
        
        # 4. Generation
        print(f"DEBUG: Chamando LLM Hugging Face via InferenceClient...")
        try:
            from huggingface_hub import InferenceClient
            
            client = InferenceClient(
                model="meta-llama/Llama-3.1-8B-Instruct",
                token=os.getenv("HUGGINGFACEHUB_API_TOKEN")
            )
            
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": context_prompt}
            ]
            
            response = ""
            for message_chunk in client.chat_completion(
                messages,
                max_tokens=1024,
                temperature=0.1,
                stream=True,
            ):
                token_text = message_chunk.choices[0].delta.content
                if token_text:
                    response += token_text
            
            print(f"DEBUG: Resposta recebida com sucesso.")
            return response
            
        except Exception as llm_err:
            print(f"ERROR: Falha no InferenceClient: {llm_err}")
            return f"Erro na IA (Inference API): {llm_err}. Verifique se o seu token tem permissões de 'Read' no Hugging Face."

    except Exception as e:
        print(f"CRITICAL ERROR em process_user_query: {e}")
        return f"Erro crítico: {str(e)}"
