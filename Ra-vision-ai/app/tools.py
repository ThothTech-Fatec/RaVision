from langchain_core.tools import tool
import requests
import json

from .database import buscar_jornada_comissao as _buscar_jornada_db

# Variável global para armazenar o token JWT recebido no request
current_auth_token = None

# ─── Helper Interno ───────────────────────────────────────────────────────────

def _build_headers() -> dict:
    """Monta os headers HTTP com Content-Type e JWT (se disponível)."""
    headers = {"Content-Type": "application/json"}
    if current_auth_token:
        headers["Authorization"] = current_auth_token
    return headers

def _validar_tipo(tipo: str) -> str | None:
    """Valida e retorna o tipo em uppercase, ou None se inválido."""
    tipos_validos = ["BONUS_FIXO", "BONUS_BASE", "OVERRIDE_PERCENTUAL", "BLACK_FRIDAY", "FAIXA_VENDAS"]
    tipo_upper = tipo.upper()
    return tipo_upper if tipo_upper in tipos_validos else None

def buscar_regra_por_id(regra_id: int) -> dict | None:
    """
    Busca os dados atuais de uma regra pelo ID.
    Retorna o dict com os campos da regra ou None se não encontrada.
    """
    try:
        response = requests.get(
            f"http://localhost:8080/api/regras/{regra_id}",
            headers=_build_headers(),
            timeout=5
        )
        if response.status_code == 200:
            return response.json()
        return None
    except Exception:
        return None


@tool
def buscar_jornada_comissao(matricula: str, date_ref: str) -> str:
    """Busca a jornada completa de cálculo de comissão de um funcionário no banco de dados.

    Use esta ferramenta SEMPRE que o usuário perguntar sobre:
    - O valor da comissão de um funcionário
    - O detalhamento ou explicação do cálculo de comissão
    - Intercorrências trabalhistas que afetaram a comissão (férias, atestado, afastamento)
    - Bônus sazonais ou campanhas aplicadas (Black Friday, Faixa de Vendas, etc.)
    - "Qual o valor final que será pago na folha?"

    A tool une as 3 tabelas de processamento sequencial do motor Java:
      1. tb_comissao_calculada_base        → comissão bruta (vendas × percentual)
      2. tb_comissao_calculada_proporcional → ajuste trabalhista (dias úteis, piso salarial)
      3. tb_comissao_calculada_final        → bônus sazonais / campanhas de RH aprovadas

    Args:
        matricula: Matrícula do funcionário (ex: "MATRIC-134"). Obrigatório.
        date_ref:  Mês de competência no formato "YYYY-MM-DD" ou "YYYY-MM"
                   (ex: "2025-11-01" para Novembro/2025). Obrigatório.

    Returns:
        String JSON com o dicionário estruturado contendo:
        - encontrado (bool): se o funcionário foi encontrado na base de cálculo
        - status_processamento: "COMPLETO", "PARCIAL_PROPORCIONAL", "APENAS_BASE" ou "NAO_ENCONTRADO"
        - etapa_base: {valor_total_vendas, percentual_comissao, valor_comissao_bruta}
        - etapa_proporcional: {dias_trabalhados, dias_intercorrencia, tipo_intercorrencia,
                               valor_comissao_ajustada} ou null se não processado
        - etapa_final: {valor_bonus_sazonal, descricao_regra_aplicada,
                        valor_comissao_definitiva} ou null se não processado
    """
    try:
        resultado = _buscar_jornada_db(matricula=matricula, date_ref=date_ref)
        return json.dumps(resultado, ensure_ascii=False, default=str)
    except Exception as e:
        erro = {
            "encontrado": False,
            "status_processamento": "ERRO",
            "mensagem_erro": str(e),
        }
        return json.dumps(erro, ensure_ascii=False)


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
    # Validar JSON de condições
    try:
        json.loads(condicoes)
    except (json.JSONDecodeError, TypeError):
        return f"Erro de validação: o campo 'condicoes' não é um JSON válido. Valor recebido: {condicoes}"

    # Validar tipo da regra
    tipo_upper = _validar_tipo(tipo)
    if not tipo_upper:
        tipos_validos = ["BONUS_FIXO", "BONUS_BASE", "OVERRIDE_PERCENTUAL", "BLACK_FRIDAY", "FAIXA_VENDAS"]
        return f"Erro de validação: tipo '{tipo}' inválido. Valores aceitos: {', '.join(tipos_validos)}"

    payload = {
        "descricaoRegra": descricao,
        "tipoRegra": tipo_upper,
        "mesCompetencia": competencia,
        "condicoesAplicacao": condicoes,
        "valorModificador": valor
    }

    try:
        response = requests.post("http://localhost:8080/api/regras", json=payload, headers=_build_headers())

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


@tool
def atualizar_regra_negocio_dinamica(
    regra_id: int,
    descricao: str,
    tipo: str,
    competencia: str,
    condicoes: str,
    valor: float
) -> str:
    """Atualiza uma regra de negócio dinâmica existente no sistema Ra Vision.

    IMPORTANTE: Use esta ferramenta apenas quando o usuário quiser ALTERAR uma regra
    já existente, informando o ID dela. Após a atualização, o status volta para PENDENTE.

    Args:
        regra_id: ID numérico da regra a ser atualizada (ex: 5).
        descricao: Nova descrição da regra.
        tipo: Tipo da regra. Deve ser um de:
            - "BONUS_FIXO", "BONUS_BASE", "OVERRIDE_PERCENTUAL", "BLACK_FRIDAY", "FAIXA_VENDAS"
        competencia: Mês de competência no formato "YYYY-MM" (ex: "2025-11").
        condicoes: String JSON com as condições de aplicação (ex: '{"matricula": "MATRIC-134"}').
        valor: Novo valor modificador numérico.
    """
    # Validar JSON de condições
    try:
        json.loads(condicoes)
    except (json.JSONDecodeError, TypeError):
        return f"Erro de validação: o campo 'condicoes' não é um JSON válido. Valor recebido: {condicoes}"

    # Validar tipo da regra
    tipo_upper = _validar_tipo(tipo)
    if not tipo_upper:
        tipos_validos = ["BONUS_FIXO", "BONUS_BASE", "OVERRIDE_PERCENTUAL", "BLACK_FRIDAY", "FAIXA_VENDAS"]
        return f"Erro de validação: tipo '{tipo}' inválido. Valores aceitos: {', '.join(tipos_validos)}"

    payload = {
        "descricaoRegra": descricao,
        "tipoRegra": tipo_upper,
        "mesCompetencia": competencia,
        "condicoesAplicacao": condicoes,
        "valorModificador": valor
    }

    try:
        response = requests.put(
            f"http://localhost:8080/api/regras/{regra_id}",
            json=payload,
            headers=_build_headers()
        )

        if response.status_code == 200:
            data = response.json()
            return (
                f"Sucesso! Regra ID {regra_id} atualizada. "
                f"Status voltou para {data.get('statusAprovacao', 'PENDENTE')} e aguarda nova aprovação do Gestor de RH."
            )
        elif response.status_code == 404:
            return f"Erro: Regra com ID {regra_id} não encontrada no sistema."
        elif response.status_code == 403:
            return "Erro de permissão (403): Você não tem autorização para editar regras."
        elif response.status_code == 401:
            return "Erro de autenticação (401): Sua sessão expirou. Por favor, faça logout e login novamente."
        else:
            return f"Falha ao atualizar regra. Status {response.status_code}. Detalhes: {response.text}"
    except requests.exceptions.ConnectionError:
        return "Erro de conexão: não foi possível conectar ao backend Java (porta 8080). Verifique se ele está rodando."
    except Exception as e:
        return f"Erro inesperado ao chamar a API Java: {str(e)}"
