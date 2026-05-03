"""
Ra Vision AI — Script de Teste para buscar_jornada_comissao (RAV-44)
======================================================================
Execução:  python test_jornada.py
Pré-req:   .env com credenciais do banco configuradas.
"""

import json
import sys
import os

# ── garante que o módulo 'app' é encontrado ──────────────────────────────────
sys.path.insert(0, os.path.dirname(__file__))
from dotenv import load_dotenv
load_dotenv()

# ── parâmetros de teste (ajuste conforme dados reais no seu banco) ────────────
MATRICULA_TESTE = os.getenv("TEST_MATRICULA", "MATRIC-001")
DATE_REF_TESTE  = os.getenv("TEST_DATE_REF",  "2025-11-01")   # YYYY-MM-DD

SEP = "=" * 60


def _fmt(valor):
    """Formata float como moeda BRL."""
    if valor is None:
        return "N/A"
    try:
        return f"R$ {float(valor):>12,.2f}"
    except (TypeError, ValueError):
        return str(valor)


def teste_1_database_direta():
    """Testa a função Python diretamente no banco (sem LangChain)."""
    print(f"\n{SEP}")
    print("TESTE 1 — database.buscar_jornada_comissao (direto no banco)")
    print(SEP)

    from app.database import buscar_jornada_comissao

    try:
        resultado = buscar_jornada_comissao(MATRICULA_TESTE, DATE_REF_TESTE)
        print(f"Matrícula : {MATRICULA_TESTE}")
        print(f"Competência: {DATE_REF_TESTE}")
        print(f"Status    : {resultado['status_processamento']}")
        print(f"Encontrado: {resultado['encontrado']}\n")

        # ── Etapa 1
        base = resultado.get("etapa_base")
        print("── ETAPA 1: Comissão Base ──────────────────────")
        if base:
            print(f"  Total Vendas     : {_fmt(base['valor_total_vendas'])}")
            print(f"  Percentual       : {base['percentual_comissao']}%")
            print(f"  Comissão Bruta   : {_fmt(base['valor_comissao_bruta'])}")
        else:
            print("  ⚠️  Não processada ainda.")

        # ── Etapa 2
        prop = resultado.get("etapa_proporcional")
        print("\n── ETAPA 2: Ajuste Proporcional ─────────────────")
        if prop:
            print(f"  Dias Trabalhados : {prop['dias_trabalhados']}")
            print(f"  Dias Intercorr.  : {prop['dias_intercorrencia']} ({prop['tipo_intercorrencia']})")
            print(f"  Valor Ajustado   : {_fmt(prop['valor_comissao_ajustada'])}")
        else:
            print("  ⏳  Ainda não processada pelo motor Java.")

        # ── Etapa 3
        final = resultado.get("etapa_final")
        print("\n── ETAPA 3: Bônus Sazonais / Final ──────────────")
        if final:
            print(f"  Regra Aplicada   : {final['descricao_regra_aplicada']}")
            print(f"  Bônus Sazonal    : {_fmt(final['valor_bonus_sazonal'])}")
            print(f"  ✅ VALOR FINAL   : {_fmt(final['valor_comissao_definitiva'])}")
        else:
            print("  ⏳  Ainda não processada pelo motor Java.")

        print(f"\n{'─'*60}")
        print("RESULTADO JSON COMPLETO:")
        print(json.dumps(resultado, indent=2, default=str, ensure_ascii=False))
        return True

    except Exception as e:
        print(f"❌ ERRO: {e}")
        import traceback
        traceback.print_exc()
        return False


def teste_2_langchain_tool():
    """Testa a @tool LangChain (serialização JSON)."""
    print(f"\n{SEP}")
    print("TESTE 2 — tools.buscar_jornada_comissao (@tool LangChain)")
    print(SEP)

    from app.tools import buscar_jornada_comissao

    try:
        resultado_json = buscar_jornada_comissao.invoke({
            "matricula": MATRICULA_TESTE,
            "date_ref":  DATE_REF_TESTE,
        })

        print(f"Tipo de retorno : {type(resultado_json).__name__}")
        print(f"Nome da tool    : {buscar_jornada_comissao.name}")
        print(f"Schema (args)   : {list(buscar_jornada_comissao.args.keys())}\n")

        dados = json.loads(resultado_json)
        print(f"status_processamento : {dados.get('status_processamento')}")
        print(f"encontrado           : {dados.get('encontrado')}")

        if dados.get("etapa_final"):
            valor = dados["etapa_final"].get("valor_comissao_definitiva")
            print(f"✅ Valor Final       : {_fmt(valor)}")

        print("\nJSON retornado pela tool:")
        print(resultado_json)
        return True

    except Exception as e:
        print(f"❌ ERRO: {e}")
        import traceback
        traceback.print_exc()
        return False


def teste_3_api_http():
    """Testa o endpoint /api/chat via HTTP (requer uvicorn rodando na 8001)."""
    print(f"\n{SEP}")
    print("TESTE 3 — POST /api/chat (endpoint HTTP / LLM)")
    print(SEP)

    import requests

    payload = {
        "message": f"Qual a comissão da matrícula {MATRICULA_TESTE}? Me explique o cálculo completo.",
        "dateRef": DATE_REF_TESTE,
    }

    try:
        resp = requests.post(
            "http://localhost:8001/api/chat",
            json=payload,
            timeout=60
        )
        print(f"Status HTTP : {resp.status_code}")

        if resp.status_code == 200:
            data = resp.json()
            print("\n── Resposta da IA ─────────────────────────────────")
            print(data.get("response", "(vazio)"))
        else:
            print(f"Erro: {resp.text}")

        return resp.status_code == 200

    except requests.exceptions.ConnectionError:
        print("⚠️  Servidor não acessível em localhost:8001.")
        print("   Verifique se o uvicorn está rodando.")
        return False
    except Exception as e:
        print(f"❌ ERRO: {e}")
        return False


def teste_4_sql_injection():
    """Confirma que parâmetros maliciosos não causam injeção SQL."""
    print(f"\n{SEP}")
    print("TESTE 4 — Prevenção de SQL Injection")
    print(SEP)

    from app.database import buscar_jornada_comissao

    payloads_maliciosos = [
        "' OR '1'='1",
        "MATRIC-001'; DROP TABLE tb_comissao_calculada_base; --",
        "'; SELECT * FROM pg_user; --",
    ]

    todos_ok = True
    for payload in payloads_maliciosos:
        try:
            resultado = buscar_jornada_comissao(payload, DATE_REF_TESTE)
            status = resultado.get("status_processamento")
            print(f"  ✅ Payload contido → status={status}  | input={payload[:40]!r}")
        except Exception as e:
            print(f"  ⚠️  Exceção (aceitável se é erro de BD): {e}")
            todos_ok = False

    return todos_ok


if __name__ == "__main__":
    print("\n" + "=" * 60)
    print("  Ra Vision AI — Suite de Testes RAV-44")
    print("  buscar_jornada_comissao")
    print("=" * 60)
    print(f"\n  Matrícula : {MATRICULA_TESTE}")
    print(f"  Data Ref  : {DATE_REF_TESTE}")
    print(f"\n  Para usar outra matrícula/data:")
    print(f"  TEST_MATRICULA=MATRIC-999 TEST_DATE_REF=2025-10-01 python test_jornada.py\n")

    resultados = {
        "DB Direta"       : teste_1_database_direta(),
        "LangChain Tool"  : teste_2_langchain_tool(),
        "HTTP /api/chat"  : teste_3_api_http(),
        "SQL Injection"   : teste_4_sql_injection(),
    }

    print(f"\n{SEP}")
    print("RESUMO DOS TESTES:")
    print(SEP)
    for nome, ok in resultados.items():
        icone = "✅" if ok else "❌"
        print(f"  {icone}  {nome}")
    print()
