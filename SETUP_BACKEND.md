# Guia de Onboarding e Setup Inicial (Backend RaVision)

Seja bem-vindo(a) ao projeto RaVision! Siga os passos abaixo para iniciar sua infraestrutura local e popular seu banco de dados pela primeira vez.

## 1. Subindo a Infraestrutura (Banco de Dados)
Este projeto utiliza PostgreSQL rodando via Docker.
Abra um terminal na raiz do backend (`Ra-vision-backend`) e execute:
```bash
docker compose up -d
```
## 2. Rodando o Java (Spring Boot)
Inicie a classe principal `RaVisionApplication` através da sua IDE de preferência ou execute localmente se possuir o Maven instalado.

## 3. População do Banco (Fluxo Padrão de API)
Como o projeto utiliza rotas de ingestão automatizadas, o seu Banco local nascerá vazio. Você precisará importar as planilhas CSV enviadas pela área de Negócios **Mês a Mês** usando uma ferramenta como o Postman ou através dos seguintes comandos de terminal:

### A) Ingestão das Planilhas de Setembro (Exemplo)
Usando sua ferramenta visual de API (Postman/Insomnia/ThunderClient), dispare:
* **Rota:** `POST http://localhost:8080/api/upload`
* **Body Type:** `multipart/form-data`
* **Campos (Form):**
  - `rh`: Anexe seu arquivo `BASE RH_SET25.csv`
  - `vendas`: Anexe seu arquivo `BASE_VENDAS_SET25.csv`
  - `comissao`: Anexe seu arquivo único `BASE_COMMISS_FINAL - Commission.csv`

*(Repita essa exata chamada `POST upload` para Outubro, Novembro, etc).*

### B) Rodando a Etapa 2 (Cálculo Base de Vendas x Cargo)
Para cada mês "upado", você deverá acionar a engine do Java que fará a primeira camada de matemática (sem proporcionalidade). Note que o EndPoint não precisa de Body, apenas o parâmetro na URL:
```bash
# Setembro
curl -X POST "http://localhost:8080/api/calculos/base?dateRef=2025-09-01"
# Outubro
curl -X POST "http://localhost:8080/api/calculos/base?dateRef=2025-10-01"
# Novembro
curl -X POST "http://localhost:8080/api/calculos/base?dateRef=2025-11-01"
```

### C) (Opcional) Cadastrando Férias
Se necessitar realizar testes na Etapa 3 bloqueando pagamentos do funcionário X:
```bash
curl -X POST "http://localhost:8080/api/intercorrencias" \
-H "Content-Type: application/json" \
--data '{ "matricula": "MATRIC-5", "tipo": "FERIAS", "dataInicio": "2025-10-10", "dataFim": "2025-10-19" }'
```

### D) Rodando a Etapa 3 (Cálculo Proporcional Definitivo e Férias)
Essa é a folha final espelhada para o Setor Financeiro. Nela, o Java abate faltas e aplica as frações exatas. Se você pular este passo, o banco final estará vazio!
```bash
# Setembro
curl -X POST "http://localhost:8080/api/calculos/proporcional?dateRef=2025-09-01"
# Outubro
curl -X POST "http://localhost:8080/api/calculos/proporcional?dateRef=2025-10-01"
# Novembro
curl -X POST "http://localhost:8080/api/calculos/proporcional?dateRef=2025-11-01"
```

### 4. Consultando Resultados
Os endpoints abaixo servem para você visualizar toda a tabela tratada (no formato JSON) para garantir que a lógica rodou perfeitamente:
* `GET http://localhost:8080/api/calculos/base?dateRef=2025-10-01`
* `GET http://localhost:8080/api/calculos/proporcional?dateRef=2025-10-01`
