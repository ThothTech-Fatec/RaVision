"""
Ra Vision AI — Parser de Intenção do Usuário
Extrai matrícula, tipo de consulta e parâmetros da mensagem.
"""

import re
from dataclasses import dataclass, field


@dataclass
class UserIntent:
    """Resultado da análise de intenção do usuário."""
    matricula: str | None = None
    cod_loja: int | None = None
    tipo_consulta: str = "geral"  # comissao, detalhamento, loja, geral
    pergunta_original: str = ""
    date_ref: str | None = None


MESES_MAP = {
    'janeiro': '01', 'feveiro': '02', 'março': '03', 'marco': '03',
    'abril': '04', 'maio': '05', 'junho': '06', 'julho': '07',
    'agosto': '08', 'setembro': '09', 'outubro': '10', 'novembro': '11',
    'dezembro': '12'
}

def parse_intent(message: str, date_ref: str | None = None) -> UserIntent:
    """
    Analisa a mensagem do usuário e extrai:
    - Matrícula (MATRIC-XXX)
    - Código de loja (LOJA-XX)
    - Data Ref (via texto ex: "em Outubro")
    - Tipo de consulta
    """
    intent = UserIntent(pergunta_original=message, date_ref=date_ref)
    msg_lower = message.lower()

    # 1. Extração de Mês (prioriza o que o usuário escreveu no chat)
    for mes_nome, mes_num in MESES_MAP.items():
        if mes_nome in msg_lower:
            # Assume 2025 para o sistema atual conforme combinado
            intent.date_ref = f"2025-{mes_num}-01"
            break

    # 2. Extrair matrícula: aceita formatos MATRIC-123, matric-123, matrícula 123, apenas 123
    matric_match = re.search(r'MATRIC[- ]?(\d+)', message, re.IGNORECASE)
    if matric_match:
        numero_clean = str(int(matric_match.group(1))) # Remove zeros à esquerda
        intent.matricula = f"MATRIC-{numero_clean}"

    # 3. Extrair loja (LOJA-10, Loja 10, etc)
    loja_match = re.search(r'LOJA[- ]?(\d+)', message, re.IGNORECASE)
    if loja_match:
        intent.cod_loja = int(loja_match.group(1))

    # 4. Determinar tipo de consulta
    #
    # Verbos de ação explícitos: necessários para detectar "criar_regra".
    # Sem eles, palavras como "bônus" ou "black friday" não são suficientes
    # para distinguir uma criação de uma simples consulta.
    CRIAR_VERBOS = [
        'criar regra', 'crie um', 'crie uma', 'cria um', 'cria uma',
        'adicionar regra', 'nova regra', 'quero criar', 'quero adicionar',
        'criar bônus', 'criar bonus', 'configurar campanha', 'adicione',
        'me crie', 'me cria', 'gere uma regra', 'gera uma regra',
    ]
    # Tipos de regra: só disparam criar_regra se combinados com verbo E sem matrícula
    CRIAR_TIPOS_REGRA = [
        'bônus fixo', 'bonus fixo', 'bônus na base', 'bonus na base',
        'override percentual', 'override de percentual', 'faixa de vendas',
        'black friday', 'regra sazonal',
    ]
    # Verbos de edição: alterar/atualizar/mudar + "regra" ou ID numérico
    EDITAR_VERBOS = [
        'alterar regra', 'atualizar regra', 'editar regra', 'modificar regra',
        'mudar regra', 'muda regra', 'altera regra', 'atualiza regra',
        'altere a regra', 'atualize a regra', 'edite a regra', 'modifique a regra',
        'altera a regra', 'atualiza a regra', 'edita a regra', 'modifica a regra',
        'muda a regra', 'mude a regra',
        'alterar o valor da regra', 'atualizar o valor da regra',
        'mudar o valor da regra', 'mudar a competência da regra',
        'alterar competência da regra', 'trocar regra',
    ]
    tem_verbo_criar = any(w in msg_lower for w in CRIAR_VERBOS)
    tem_tipo_regra = any(w in msg_lower for w in CRIAR_TIPOS_REGRA)
    tem_verbo_editar = any(w in msg_lower for w in EDITAR_VERBOS)

    # Detecção adicional de edição: verbo de modificação + "id" + número
    tem_id_numerico = bool(re.search(r'\b(?:id\s+|regra\s+(?:de\s+)?(?:id\s+)?)(\d+)', msg_lower))
    tem_verbo_modificacao = any(w in msg_lower for w in ['alterar', 'atualizar', 'editar', 'modificar', 'mudar', 'mude', 'altere', 'atualize', 'troque'])
    if not tem_verbo_editar and tem_verbo_modificacao and tem_id_numerico:
        tem_verbo_editar = True

    if any(w in msg_lower for w in ['quem é', 'quem e', 'gerente', 'responsável', 'responsavel']):
        intent.tipo_consulta = "gerente"
    elif any(w in msg_lower for w in ['comissão', 'comissao', 'valor', 'quanto', 'calcul']):
        intent.tipo_consulta = "comissao"
    elif any(w in msg_lower for w in ['detalh', 'expliq', 'como', 'por que', 'porque', 'passo']):
        intent.tipo_consulta = "detalhamento"
    elif tem_verbo_editar:
        # Edição tem prioridade sobre criação quando há verbo explícito de modificação
        intent.tipo_consulta = "editar_regra"
    elif tem_verbo_criar or (tem_tipo_regra and not intent.matricula and not intent.cod_loja):
        # Só classifica como criar_regra se houver verbo de ação explícito
        # OU se houver tipo de regra SEM matrícula/loja (para não confundir com consulta)
        intent.tipo_consulta = "criar_regra"
    elif any(w in msg_lower for w in ['loja', 'equipe', 'time', 'todos']):
        intent.tipo_consulta = "loja"
    elif intent.matricula:
        intent.tipo_consulta = "comissao"

    return intent

