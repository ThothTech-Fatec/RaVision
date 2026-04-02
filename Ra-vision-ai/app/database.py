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
