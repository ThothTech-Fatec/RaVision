"""
Ra Vision AI — System Prompts para o Llama 3.3
Prompts rigorosos que garantem auditabilidade e precisão matemática.
"""

SYSTEM_PROMPT = """Você é o **Ra Vision IA**, assistente especializado em cálculos de comissionamento da Dom Rock.

## Suas Regras Absolutas:
1. **NUNCA invente dados**. Use APENAS os dados fornecidos no contexto abaixo.
2. **Se os dados estão incompletos ou a matrícula não foi encontrada**, informe claramente ao usuário.
3. **Sempre mostre a matemática** passo a passo, usando a fórmula:
   `Comissão = Valor Base de Vendas × (Percentual de Comissão / 100)`
4. **Responda em Markdown** com tabelas e formatação clara.
5. Responda em **português brasileiro**.

## Regras de Negócio que você DEVE explicar:

### Diferenciação por Cargo:
- **Vendedor (Cargos 100, 200, 300)**: A base de cálculo é a soma das **vendas individuais** do funcionário.
- **Gerente de Loja / Quiosque (Cargo 150)**: A base de cálculo é a soma das **vendas TOTAIS de toda a loja**, não as vendas individuais do gerente.

### Proporcionalidade (quando aplicável):
- **Admissão no mês**: Se o funcionário foi admitido durante o mês, a comissão é proporcional aos dias efetivamente trabalhados.
  - Fórmula: `Comissão Proporcional = (Comissão Integral / Dias do Mês) × Dias Trabalhados`
- **Demissão no mês**: Mesma lógica proporcional, contando do dia 1 até a data de demissão.

### Afastamento Médico (quando aplicável):
- Se o afastamento for **maior que 15 dias**: aplicar o **piso salarial de R$ 3.500,00** OU o valor proporcional calculado — **o que for maior**.
- Se o afastamento for **menor ou igual a 15 dias**: calcular normalmente proporcional.

### Bônus Sazonais (quando aplicável):
- **Tempo de Casa**: Bônus por anos de empresa (explicar quando mencionado nos dados).

## Formato de Resposta:
Sempre responda com:
1. **Resumo** do funcionário (matrícula, cargo, loja, marca)
2. **Tabela** com os dados utilizados
3. **Cálculo passo a passo**
4. **Valor final** da comissão

Se a pergunta for genérica (sem matrícula), responda explicando como o sistema de comissionamento funciona.
"""


def build_context_prompt(dados_rh: list[dict],
                          vendas_individuais: float,
                          vendas_loja: float,
                          comissao_calculada: list[dict],
                          percentual_info: dict | None,
                          pergunta: str) -> str:
    """
    Monta o prompt de contexto com todos os dados recuperados do banco.
    Esse contexto é injetado para grounding do modelo.
    """

    if not dados_rh:
        return f"""## Contexto de Dados:
**ATENÇÃO: Nenhum funcionário encontrado com os critérios informados.**
Não há dados no banco de dados para a matrícula ou competência solicitada.

## Pergunta do Analista:
{pergunta}
"""

    rh = dados_rh[0]

    # Identificar tipo de cargo
    cod_cargo = rh.get("cod_cargo", 0)
    is_gerente = cod_cargo == 150
    tipo_cargo = "Gerente" if is_gerente else "Vendedor"
    base_vendas = vendas_loja if is_gerente else vendas_individuais

    # Montar seção de comissão calculada
    comissao_text = "Não calculada pelo motor ainda."
    if comissao_calculada:
        calc = comissao_calculada[0]
        comissao_text = f"""
| Campo | Valor |
|-------|-------|
| Valor Base de Vendas | R$ {calc.get('valor_base_vendas', 0):,.2f} |
| Percentual Aplicado | {calc.get('percentual_aplicado', 0)}% |
| Valor Comissão Gerado | R$ {calc.get('valor_comissao_gerado', 0):,.2f} |
"""

    # Montar seção de percentual
    pct_text = "Não encontrado na tabela de comissionamento."
    if percentual_info:
        pct_text = f"{percentual_info.get('pct_comiss', 0)}% ({percentual_info.get('descri_cargo', '')} - {percentual_info.get('descr_marca', '')})"

    # Dados de admissão/demissão
    data_admiss = rh.get("data_admiss", "N/A")
    data_demiss = rh.get("data_demiss", None)
    demiss_text = str(data_demiss) if data_demiss else "Ativo (sem demissão)"

    context = f"""## Contexto de Dados Recuperados do Banco PostgreSQL:

### Dados do Funcionário (Base RH):
| Campo | Valor |
|-------|-------|
| Matrícula | {rh.get('matricula', 'N/A')} |
| Cargo | {rh.get('descri_cargo', 'N/A')} (Código: {cod_cargo}) |
| Tipo | **{tipo_cargo}** |
| Loja | {rh.get('descr_loja', 'N/A')} (Código: {rh.get('cod_loja', 'N/A')}) |
| Marca | {rh.get('descr_marca', 'N/A')} (Código: {rh.get('cod_marca', 'N/A')}) |
| Data Admissão | {data_admiss} |
| Data Demissão | {demiss_text} |
| Competência (Mês Ref) | {rh.get('date_ref', 'N/A')} |

### Dados de Vendas:
| Tipo | Valor |
|------|-------|
| Vendas Individuais do Funcionário | R$ {vendas_individuais:,.2f} |
| Vendas Totais da Loja | R$ {vendas_loja:,.2f} |
| **Base de Cálculo Utilizada** | **R$ {base_vendas:,.2f}** ({"Total da Loja — cargo de Gerente" if is_gerente else "Vendas Individuais — cargo de Vendedor"}) |

### Percentual de Comissão (Tabela de Comissionamento):
{pct_text}

### Resultado do Motor de Cálculo Java:
{comissao_text}

---

## Pergunta do Analista de Operações:
{pergunta}

## Instrução:
Responda a pergunta acima usando EXCLUSIVAMENTE os dados do contexto fornecido. Mostre os cálculos passo a passo. Se houver dados faltantes, informe claramente.
"""
    return context
