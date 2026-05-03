"""Inspeciona o schema real das 3 tabelas de comissao no banco."""
from dotenv import load_dotenv
load_dotenv()

from app.database import get_session
from sqlalchemy import text

TABELAS = [
    "tb_comissao_calculada_base",
    "tb_comissao_calculada_proporcional",
    "tb_comissao_calculada_final",
]

with get_session() as s:
    for tabela in TABELAS:
        r = s.execute(
            text("""
                SELECT column_name, data_type
                FROM information_schema.columns
                WHERE table_name = :t
                ORDER BY ordinal_position
            """),
            {"t": tabela}
        )
        cols = r.mappings().all()
        print(f"\n=== {tabela} ===")
        if not cols:
            print("  (tabela nao encontrada ou sem colunas)")
        for c in cols:
            print(f"  {c['column_name']:<40} {c['data_type']}")
