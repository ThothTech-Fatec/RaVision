import os
import json
from langchain_huggingface import HuggingFaceEndpoint, ChatHuggingFace
from langchain_core.prompts import ChatPromptTemplate, SystemMessagePromptTemplate, HumanMessagePromptTemplate
from langchain_core.output_parsers import StrOutputParser

from .database import (
    buscar_funcionario_por_matricula,
    buscar_total_vendas_funcionario,
    buscar_vendas_loja,
    buscar_comissao_calculada,
    buscar_percentual_comissao,
    buscar_gerentes_loja
)
from .intent_parser import parse_intent
from .prompts import SYSTEM_PROMPT, build_context_prompt
from .tools import criar_regra_negocio_dinamica

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
    import re
    msg_lower = message.lower()
    
    params = {
        "tipo": None,
        "competencia": None,
        "valor": None,
        "condicoes": None,
        "descricao": None,
    }
    
    # Detectar tipo
    tipo_map = {
        "bonus_fixo": ["bônus fixo", "bonus fixo", "bônus nominal", "bonus nominal"],
        "BONUS_BASE": ["bônus na base", "bonus na base", "bônus base", "bonus base"],
        "OVERRIDE_PERCENTUAL": ["override", "substituição", "substituicao", "alterar percentual", "mudar comissão"],
        "BLACK_FRIDAY": ["black friday", "blackfriday"],
        "FAIXA_VENDAS": ["faixa de vendas", "faixa vendas", "escalonado", "performance"],
    }
    for tipo_key, keywords in tipo_map.items():
        if any(kw in msg_lower for kw in keywords):
            params["tipo"] = tipo_key.upper()
            break
    
    # Detectar valor
    valor_match = re.search(r'R\$\s*([\d.,]+)', message)
    if not valor_match:
        valor_match = re.search(r'(\d+[.,]?\d*)\s*(?:reais|real)', msg_lower)
    if not valor_match:
        valor_match = re.search(r'(?:valor|bônus|bonus|de)\s+(?:de\s+)?(?:R\$\s*)?([\d.,]+)', message, re.IGNORECASE)
    if valor_match:
        valor_str = valor_match.group(1).replace(".", "").replace(",", ".")
        try:
            params["valor"] = float(valor_str)
        except ValueError:
            pass
    
    # Detectar matrícula
    matric_match = re.search(r'MATRIC[- ]?(\d+)', message, re.IGNORECASE)
    if matric_match:
        params["condicoes"] = json.dumps({"matricula": f"MATRIC-{int(matric_match.group(1))}"})
    
    # Detectar cargo
    cargo_match = re.search(r'cargo\s+(\d+)', msg_lower)
    if cargo_match and not params["condicoes"]:
        params["condicoes"] = json.dumps({"codCargo": int(cargo_match.group(1))})
    
    # Detectar marca
    marca_match = re.search(r'marca\s+(\d+)', msg_lower)
    if marca_match and not params["condicoes"]:
        params["condicoes"] = json.dumps({"codMarca": int(marca_match.group(1))})
    
    # Detectar competência (mês)
    from .intent_parser import MESES_MAP
    for mes_nome, mes_num in MESES_MAP.items():
        if mes_nome in msg_lower:
            ano_match = re.search(r'20\d{2}', message)
            ano = ano_match.group(0) if ano_match else "2025"
            params["competencia"] = f"{ano}-{mes_num}"
            break
    
    if not params["competencia"] and date_ref:
        # Não forçar — deixar None para que a IA pergunte
        pass

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
        # Fluxo RAG padrão (consultas de comissão, detalhamento, etc)
        # ─────────────────────────────────────────────────────────
        
        # 2. Retrieval
        context_data = {
            "dados_rh": [],
            "vendas_individuais": 0.0,
            "vendas_loja": 0.0,
            "comissao_calculada": [],
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
            context_data["percentual_info"] = buscar_percentual_comissao(
                rh_principal.get("cod_marca"), 
                rh_principal.get("cod_cargo")
            )
            print(f"DEBUG: Enriquecimento concluído para {matricula_alvo}.")
        else:
            print(f"DEBUG: Nenhum dado de RH encontrado para os critérios.")
                
        # 3. Build Context Prompt
        context_prompt = build_context_prompt(
            dados_rh=context_data["dados_rh"],
            vendas_individuais=context_data["vendas_individuais"],
            vendas_loja=context_data["vendas_loja"],
            comissao_calculada=context_data["comissao_calculada"],
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
