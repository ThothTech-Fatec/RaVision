"""Testa _extrair_parametros_regra para os 5 casos problemáticos."""
import json, sys
sys.path.insert(0, '.')
from dotenv import load_dotenv; load_dotenv()
import importlib, app.rag_chain as rc
importlib.reload(rc)

casos = [
    (
        "OVERRIDE com matricula",
        "Preciso alterar o percentual de comissao da matricula MATRIC-400 para 8.5% em novembro de 2025",
        {"tipo": "OVERRIDE_PERCENTUAL", "valor": 8.5, "condicoes_key": "matricula", "competencia": "2025-11"},
    ),
    (
        "BONUS_BASE cargo 200",
        "Preciso somar R$ 20.000 na base de calculo de todos os funcionarios do cargo 200 em novembro de 2025",
        {"tipo": "BONUS_BASE", "valor": 20000.0, "condicoes_key": "codCargo", "competencia": "2025-11"},
    ),
    (
        "BLACK_FRIDAY para todos 1pct",
        "Quero criar uma regra de Black Friday com acrescimo de 1% para todos os funcionarios em novembro de 2025",
        {"tipo": "BLACK_FRIDAY", "valor": 1.0, "condicoes_key": None, "competencia": "2025-11"},
    ),
    (
        "FAIXA_VENDAS cargo 100 meta 50k bonus 4k",
        "Criar regra de faixa de vendas: quem do cargo 100 vender acima de R$ 50.000 recebe R$ 4.000 de bonus em novembro 2025",
        {"tipo": "FAIXA_VENDAS", "valor": 4000.0, "condicoes_key": "codCargo", "competencia": "2025-11"},
    ),
    (
        "OVERRIDE marca 10 6pct",
        "Aplicar override de percentual de 6% para todos os funcionarios da marca 10 em novembro de 2025",
        {"tipo": "OVERRIDE_PERCENTUAL", "valor": 6.0, "condicoes_key": "codMarca", "competencia": "2025-11"},
    ),
]

print("Caso                                       Tipo              Valor      Cond         Mes      OK?")
print("-" * 110)

todos_ok = True
for desc, msg, esperado in casos:
    p = rc._extrair_parametros_regra(msg, "2025-11-01")
    cond = json.loads(p["condicoes"]) if p["condicoes"] else {}

    tipo_ok  = p["tipo"] == esperado["tipo"]
    valor_ok = p["valor"] == esperado["valor"]
    comp_ok  = p["competencia"] == esperado["competencia"]
    cond_ok  = (cond == {}) if esperado["condicoes_key"] is None else (esperado["condicoes_key"] in cond)

    ok = tipo_ok and valor_ok and comp_ok and cond_ok
    if not ok:
        todos_ok = False
    sinal = "OK" if ok else f"ERRO tipo={tipo_ok} val={valor_ok} cond={cond_ok} comp={comp_ok}"
    print(f"{desc:<42} {str(p['tipo']):<17} {str(p['valor']):<10} {str(cond):<25} {str(p['competencia']):<8} {sinal}")

print()
print("Resultado:", "TODOS OK" if todos_ok else "HA FALHAS")
