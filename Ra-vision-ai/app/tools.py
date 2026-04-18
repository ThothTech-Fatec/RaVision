from langchain_core.tools import tool
import requests
import json

# Variável global ou contextvar para armazenar o token JWT recebido no request
current_auth_token = None

@tool
def criar_regra_negocio_dinamica(descricao: str, tipo: str, competencia: str, condicoes: str, valor: float) -> str:
    """
    Cria uma nova regra de negócio dinâmica no sistema através da API do Java.
    
    Args:
        descricao: Uma descrição clara do que a regra faz.
        tipo: O tipo da regra. Deve ser EXATAMENTE UM destes valores: "OVERRIDE_PERCENTUAL", "BONUS_FIXO", "FAIXA_VENDAS", "BLACK_FRIDAY".
        competencia: O mês de competência no formato "YYYY-MM" (ex: "2025-08").
        condicoes: Um JSON em formato de string com as condições (ex: "{\"matricula\": \"MATRIC-134\"}").
        valor: O valor modificador numérico (ex: 500.0 ou 1.75).
    """
    url = "http://localhost:8080/api/regras"
    
    headers = {
        "Content-Type": "application/json"
    }
    
    if current_auth_token:
        headers["Authorization"] = current_auth_token
        
    payload = {
        "descricaoRegra": descricao,
        "tipoRegra": tipo,
        "mesCompetencia": competencia,
        "condicoesAplicacao": condicoes,
        "valorModificador": valor
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        
        if response.status_code == 201:
            data = response.json()
            return f"Sucesso! Regra criada com ID {data.get('id')} e status {data.get('statusAprovacao')}. Ela aguarda aprovação do Gestor de RH."
        else:
            return f"Falha ao criar regra. Status {response.status_code}. Detalhes: {response.text}"
    except Exception as e:
        return f"Erro de conexão com a API Java: {str(e)}"
