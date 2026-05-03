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
    CRIAR_VERBOS = [
        'criar regra', 'crie um', 'crie uma', 'cria um', 'cria uma',
        'adicionar regra', 'nova regra', 'quero criar', 'quero adicionar',
        'criar bônus', 'criar bonus', 'configurar campanha', 'adicione',
        'me crie', 'me cria', 'gere uma regra', 'gera uma regra',
        # OVERRIDE_PERCENTUAL
        'alterar percentual', 'mudar percentual', 'mudar a taxa',
        'alterar a taxa', 'alterar o percentual', 'mudar o percentual',
        'definir percentual', 'configurar percentual', 'definir taxa',
        'aplicar percentual', 'trocar percentual', 'novo percentual',
        'mudar comissão', 'mudar comissao', 'alterar comissão', 'alterar comissao',
        # BLACK_FRIDAY / geral
        'preciso criar', 'preciso adicionar', 'vou criar', 'quero configurar',
        'criar campanha', 'criar uma campanha', 'adicionar campanha',
        # BONUS
        'dar um bônus', 'dar bonus', 'dar bônus', 'conceder bônus',
        'conceder bonus', 'quero dar', 'somar na base', 'somar bônus',
        'preciso somar', 'preciso dar', 'preciso aplicar', 'quero somar',
        # FAIXA_VENDAS
        'criar faixa', 'nova faixa', 'regra de meta', 'meta de vendas',
        'se atingir', 'se vender acima', 'se vender mais',
    ]
    # Tipos de regra: só disparam criar_regra se combinados com verbo
    CRIAR_TIPOS_REGRA = [
        'bônus fixo', 'bonus fixo', 'bônus na base', 'bonus na base',
        'override percentual', 'override de percentual', 'faixa de vendas',
        'faixa vendas', 'black friday', 'regra sazonal', 'campanha de rh',
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
    tem_tipo_regra  = any(w in msg_lower for w in CRIAR_TIPOS_REGRA)
    tem_verbo_editar = any(w in msg_lower for w in EDITAR_VERBOS)

    # Detecção adicional de edição: verbo de modificação + "id" + número
    tem_id_numerico = bool(re.search(r'\b(?:id\s+|regra\s+(?:de\s+)?(?:id\s+)?)(\d+)', msg_lower))
    tem_verbo_modificacao = any(w in msg_lower for w in ['alterar', 'atualizar', 'editar', 'modificar', 'mudar', 'mude', 'altere', 'atualize', 'troque'])
    if not tem_verbo_editar and tem_verbo_modificacao and tem_id_numerico:
        tem_verbo_editar = True

    # Detecção de OVERRIDE_PERCENTUAL: verbo de mudança + valor percentual explícito (ex: 8.5%)
    # Isso captura "alterar percentual da MATRIC-X para 8.5%" sem precisar de ID de regra
    tem_override_create = bool(
        re.search(r'\d+[.,]?\d*\s*%', msg_lower)  # tem um valor em % na mensagem
        and any(w in msg_lower for w in [
            'alterar', 'mudar', 'aplicar', 'definir', 'configurar',
            'trocar', 'preciso', 'quero', 'setar', 'fixar',
        ])
        and any(w in msg_lower for w in [
            'percentual', 'taxa', 'comissão', 'comissao', 'override',
        ])
        and not tem_id_numerico  # evita confundir com edição de regra existente
    )
    if tem_override_create:
        tem_verbo_criar = True

    # ── Classificação final de intenção ─────────────────────────────────────
    # ORDEM IMPORTANTE: criar/editar ANTES de comissao/detalhamento
    # para evitar que frases como "alterar percentual" caiam no fluxo RAG.

    if any(w in msg_lower for w in ['quem é', 'quem e', 'gerente', 'responsável', 'responsavel']):
        intent.tipo_consulta = "gerente"
    elif tem_verbo_editar:
        # Edição tem prioridade absoluta quando há verbo explícito de modificação de regra
        intent.tipo_consulta = "editar_regra"
    elif tem_verbo_criar or (tem_tipo_regra and not intent.cod_loja):
        # Criação: verbo de ação explícito OU tipo de regra sem loja
        # (permite matric + verbo criar para BONUS_FIXO/OVERRIDE de uma pessoa específica)
        intent.tipo_consulta = "criar_regra"
    elif any(w in msg_lower for w in ['detalh', 'expliq', 'como', 'por que', 'porque', 'passo', 'jornada']):
        intent.tipo_consulta = "detalhamento"
    elif any(w in msg_lower for w in ['comissão', 'comissao', 'valor', 'quanto', 'calcul']):
        intent.tipo_consulta = "comissao"
    elif any(w in msg_lower for w in ['loja', 'equipe', 'time', 'todos']):
        intent.tipo_consulta = "loja"
    elif intent.matricula:
        intent.tipo_consulta = "comissao"

    return intent

