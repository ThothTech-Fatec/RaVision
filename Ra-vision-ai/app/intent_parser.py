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
    if any(w in msg_lower for w in ['quem é', 'quem e', 'gerente', 'responsável', 'responsavel']):
        intent.tipo_consulta = "gerente"
    elif any(w in msg_lower for w in ['comissão', 'comissao', 'valor', 'quanto', 'calcul']):
        intent.tipo_consulta = "comissao"
    elif any(w in msg_lower for w in ['detalh', 'expliq', 'como', 'por que', 'porque', 'passo']):
        intent.tipo_consulta = "detalhamento"
    elif any(w in msg_lower for w in ['loja', 'equipe', 'time', 'todos']):
        intent.tipo_consulta = "loja"
    elif intent.matricula:
        intent.tipo_consulta = "comissao"

    return intent
