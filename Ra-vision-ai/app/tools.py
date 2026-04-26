from langchain_core.tools import tool
import requests
import json

# Variável global para armazenar o token JWT recebido no request
current_auth_token = None


@tool
def criar_regra_negocio_dinamica(
    descricao: str,
    tipo: str,
    competencia: str,
    condicoes: str,
    valor: float
) -> str:
    """Cria uma nova regra de negócio dinâmica no sistema Ra Vision.

    IMPORTANTE: Só chame esta ferramenta quando TODOS os parâmetros estiverem
    confirmados pelo usuário. Se faltar qualquer um, pergunte antes.

    Args:
        descricao: Descrição clara e humana do que a regra faz.
            Exemplo: "Bônus Fixo de R$ 500 para MATRIC-134 em Agosto/2025"
        tipo: O tipo EXATO da regra. Deve ser EXATAMENTE um destes valores:
            - "BONUS_FIXO" — Bônus fixo nominal somado na comissão final.
            - "BONUS_BASE" — Valor somado na base de vendas antes do cálculo do percentual.
            - "OVERRIDE_PERCENTUAL" — Substitui a taxa de comissão do funcionário.
            - "BLACK_FRIDAY" — Acréscimo percentual para a semana de Black Friday.
            - "FAIXA_VENDAS" — Bônus escalonado por faixa mínima de vendas atingida.
        competencia: O mês de competência no formato "YYYY-MM".
            Exemplo: "2025-08" para Agosto de 2025.
        condicoes: Uma string contendo um JSON válido com as condições de aplicação.
            Exemplos válidos:
            - '{"matricula": "MATRIC-134"}' — aplica apenas para esta matrícula
            - '{"codCargo": 200}' — aplica para todos do cargo 200
            - '{"codMarca": 10}' — aplica para todos da marca 10
            - '{"minVendas": 50000}' — para FAIXA_VENDAS, define a meta mínima
            - '{}' — aplica para TODOS os funcionários
        valor: O valor modificador numérico:
            - Para BONUS_FIXO/BONUS_BASE: valor em Reais (ex: 500.0)
            - Para OVERRIDE_PERCENTUAL: novo percentual (ex: 8.5)
            - Para BLACK_FRIDAY: acréscimo percentual (ex: 1.0 para +1%)
            - Para FAIXA_VENDAS: valor do bônus em Reais ao atingir a faixa
    """
    url = "http://localhost:8080/api/regras"
    
    headers = {
        "Content-Type": "application/json"
    }
    
    if current_auth_token:
        headers["Authorization"] = current_auth_token

    # Validar o JSON de condições antes de enviar
    try:
        json.loads(condicoes)
    except (json.JSONDecodeError, TypeError):
        return f"Erro de validação: o campo 'condicoes' não é um JSON válido. Valor recebido: {condicoes}"

    # Validar o tipo da regra
    tipos_validos = ["BONUS_FIXO", "BONUS_BASE", "OVERRIDE_PERCENTUAL", "BLACK_FRIDAY", "FAIXA_VENDAS"]
    tipo_upper = tipo.upper()
    if tipo_upper not in tipos_validos:
        return f"Erro de validação: tipo '{tipo}' inválido. Valores aceitos: {', '.join(tipos_validos)}"
        
    payload = {
        "descricaoRegra": descricao,
        "tipoRegra": tipo_upper,
        "mesCompetencia": competencia,
        "condicoesAplicacao": condicoes,
        "valorModificador": valor
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        
        if response.status_code == 201:
            data = response.json()
            return f"Sucesso! Regra criada com ID {data.get('id')} e status {data.get('statusAprovacao')}. Ela aguarda aprovação do Gestor de RH."
        elif response.status_code == 403:
            return "Erro de permissão (403): Você não tem autorização para criar regras. Verifique se está logado como ADMINISTRADOR ou GESTOR_RH."
        elif response.status_code == 401:
            return "Erro de autenticação (401): Sua sessão expirou. Por favor, faça logout e login novamente."
        else:
            return f"Falha ao criar regra. Status {response.status_code}. Detalhes: {response.text}"
    except requests.exceptions.ConnectionError:
        return "Erro de conexão: não foi possível conectar ao backend Java (porta 8080). Verifique se ele está rodando."
    except Exception as e:
        return f"Erro inesperado ao chamar a API Java: {str(e)}"
