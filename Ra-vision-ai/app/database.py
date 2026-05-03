"""
Ra Vision AI — Módulo de Conexão com PostgreSQL
Queries parametrizadas para prevenção de SQL Injection.
"""

import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv

load_dotenv()

DATABASE_URL = (
    f"postgresql://{os.getenv('DB_USER', 'postgres')}:{os.getenv('DB_PASSWORD', 'password')}"
    f"@{os.getenv('DB_HOST', 'localhost')}:{os.getenv('DB_PORT', '5432')}"
    f"/{os.getenv('DB_NAME', 'ravision_db')}"
)

engine = create_engine(DATABASE_URL, pool_size=5, max_overflow=10)
SessionLocal = sessionmaker(bind=engine)


def get_session():
    return SessionLocal()


def buscar_funcionario_por_matricula(matricula: str, date_ref: str) -> list[dict]:
    """Busca dados de RH de um funcionário por matrícula e competência."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT id, date_ref, cod_marca, descr_marca, cod_loja, descr_loja,
                       matricula, data_admiss, data_demiss, cod_cargo, descr_cargo
                FROM tb_base_rh
                WHERE matricula = :matricula AND date_ref = :date_ref
            """),
            {"matricula": matricula, "date_ref": date_ref}
        )
        rows = result.mappings().all()
        return [dict(r) for r in rows]


def buscar_vendas_funcionario(matricula: str, date_ref: str) -> list[dict]:
    """Busca todas as vendas individuais de um funcionário na competência."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT id, date_ref, cod_marca, descr_marca, cod_loja, descr_loja,
                       matricula, vlr_venda
                FROM tb_base_vendas
                WHERE matricula = :matricula AND date_ref = :date_ref
            """),
            {"matricula": matricula, "date_ref": date_ref}
        )
        rows = result.mappings().all()
        return [dict(r) for r in rows]


def buscar_total_vendas_funcionario(matricula: str, date_ref: str) -> float:
    """Soma total das vendas individuais do funcionário."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT COALESCE(SUM(vlr_venda), 0) as total
                FROM tb_base_vendas
                WHERE matricula = :matricula AND date_ref = :date_ref
            """),
            {"matricula": matricula, "date_ref": date_ref}
        )
        row = result.mappings().first()
        return float(row["total"]) if row else 0.0


def buscar_vendas_loja(cod_loja: int, date_ref: str) -> float:
    """Soma total das vendas de uma loja inteira (usado para Gerentes)."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT COALESCE(SUM(vlr_venda), 0) as total
                FROM tb_base_vendas
                WHERE cod_loja = :cod_loja AND date_ref = :date_ref
            """),
            {"cod_loja": cod_loja, "date_ref": date_ref}
        )
        row = result.mappings().first()
        return float(row["total"]) if row else 0.0


def buscar_jornada_comissao(matricula: str, date_ref: str) -> dict:
    """
    Busca a jornada completa de cálculo de comissão de um funcionário,
    unindo as 3 tabelas de processamento sequencial via LEFT JOIN.

    Retorna um dicionário estruturado com:
      - etapa_base        : dados da comissão bruta inicial
      - etapa_proporcional: dados do ajuste trabalhista (pode ser None)
      - etapa_final       : dados dos bônus sazonais e valor definitivo (pode ser None)
      - status_processamento: string descritiva do estágio atual do cálculo
    """
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT
                    -- ── Etapa 1: Base ──────────────────────────────────────────
                    b.valor_base_vendas          AS base_valor_total_vendas,
                    b.percentual_aplicado         AS base_percentual_comissao,
                    b.valor_comissao_gerado       AS base_valor_comissao_bruta,
                    b.cod_loja                    AS base_cod_loja,
                    b.cod_cargo                   AS base_cod_cargo,

                    -- ── Etapa 2: Proporcional (Intercorrências Trabalhistas) ───
                    p.dias_do_mes                 AS prop_dias_do_mes,
                    p.dias_trabalhados            AS prop_dias_trabalhados,
                    (p.dias_do_mes - p.dias_trabalhados)
                                                  AS prop_dias_intercorrencia,
                    p.motivo_proporcionalidade    AS prop_motivo_proporcionalidade,
                    p.valor_comissao_proporcional AS prop_valor_comissao_ajustada,

                    -- ── Etapa 3: Final (Bônus Sazonais / Campanhas) ───────────
                    f.historico_regras_aplicadas  AS final_historico_regras,
                    f.valor_comissao_final        AS final_valor_comissao_definitiva

                FROM tb_comissao_calculada_base b
                LEFT JOIN tb_comissao_calculada_proporcional p
                    ON p.matricula = b.matricula AND p.date_ref = b.date_ref
                LEFT JOIN tb_comissao_calculada_final f
                    ON f.matricula = b.matricula AND f.date_ref = b.date_ref
                WHERE b.matricula = :matricula
                  AND b.date_ref  = :date_ref
                LIMIT 1
            """),
            {"matricula": matricula, "date_ref": date_ref}
        )
        row = result.mappings().first()

    if not row:
        return {
            "encontrado": False,
            "etapa_base": None,
            "etapa_proporcional": None,
            "etapa_final": None,
            "status_processamento": "NAO_ENCONTRADO",
        }

    row = dict(row)

    etapa_base = {
        "valor_total_vendas":   row.get("base_valor_total_vendas"),
        "percentual_comissao":  row.get("base_percentual_comissao"),
        "valor_comissao_bruta": row.get("base_valor_comissao_bruta"),
        "cod_loja":             row.get("base_cod_loja"),
        "cod_cargo":            row.get("base_cod_cargo"),
    }

    # Etapa proporcional pode nao ter sido executada ainda
    has_prop = row.get("prop_valor_comissao_ajustada") is not None
    etapa_proporcional = {
        "dias_do_mes":            row.get("prop_dias_do_mes"),
        "dias_trabalhados":       row.get("prop_dias_trabalhados"),
        "dias_intercorrencia":    row.get("prop_dias_intercorrencia"),
        "motivo_proporcionalidade": row.get("prop_motivo_proporcionalidade"),
        "valor_comissao_ajustada":  row.get("prop_valor_comissao_ajustada"),
    } if has_prop else None

    # Etapa final pode nao ter sido executada ainda
    has_final = row.get("final_valor_comissao_definitiva") is not None
    etapa_final = {
        "historico_regras_aplicadas": row.get("final_historico_regras"),
        "valor_comissao_definitiva":  row.get("final_valor_comissao_definitiva"),
    } if has_final else None

    # Determinar status do processamento
    if has_final:
        status = "COMPLETO"
    elif has_prop:
        status = "PARCIAL_PROPORCIONAL"
    else:
        status = "APENAS_BASE"

    return {
        "encontrado": True,
        "matricula": matricula,
        "date_ref": date_ref,
        "etapa_base": etapa_base,
        "etapa_proporcional": etapa_proporcional,
        "etapa_final": etapa_final,
        "status_processamento": status,
    }


def buscar_comissao_calculada(matricula: str, date_ref: str) -> list[dict]:
    """Busca o cálculo de comissão já processado pelo motor Java."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT id, date_ref, matricula, cod_loja, cod_cargo,
                       valor_base_vendas, percentual_aplicado, valor_comissao_gerado
                FROM tb_comissao_calculada_base
                WHERE matricula = :matricula AND date_ref = :date_ref
            """),
            {"matricula": matricula, "date_ref": date_ref}
        )
        rows = result.mappings().all()
        return [dict(r) for r in rows]


def buscar_percentual_comissao(cod_marca: int, cod_cargo: int) -> dict | None:
    """Busca o percentual de comissão pela chave marca+cargo."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT cod_marca, descr_marca, cod_cargo, descri_cargo, pct_comiss
                FROM tb_base_comissionamento
                WHERE cod_marca = :cod_marca AND cod_cargo = :cod_cargo
                LIMIT 1
            """),
            {"cod_marca": cod_marca, "cod_cargo": cod_cargo}
        )
        row = result.mappings().first()
        return dict(row) if row else None


def buscar_gerentes_loja(cod_loja: int, date_ref: str) -> list[dict]:
    """Busca os gerentes (cod_cargo=150) de uma loja na competência."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT matricula, cod_cargo, descr_cargo, cod_loja, descr_loja
                FROM tb_base_rh
                WHERE cod_loja = :cod_loja 
                  AND date_ref = :date_ref 
                  AND (cod_cargo = 150 OR descr_cargo ILIKE '%GERENTE%')
            """),
            {"cod_loja": cod_loja, "date_ref": date_ref}
        )
        rows = result.mappings().all()
        return [dict(r) for r in rows]


def buscar_funcionarios_loja(cod_loja: int, date_ref: str) -> list[dict]:
    """Lista todos os funcionários de uma loja na competência."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT matricula, cod_cargo, descr_cargo
                FROM tb_base_rh
                WHERE cod_loja = :cod_loja AND date_ref = :date_ref
            """),
            {"cod_loja": cod_loja, "date_ref": date_ref}
        )
        rows = result.mappings().all()
        return [dict(r) for r in rows]


def buscar_competencias_disponiveis() -> list[str]:
    """Retorna todas as competências (meses) disponíveis no banco."""
    with get_session() as session:
        result = session.execute(
            text("""
                SELECT DISTINCT date_ref FROM tb_base_rh ORDER BY date_ref DESC
            """)
        )
        rows = result.mappings().all()
        return [str(r["date_ref"]) for r in rows]
