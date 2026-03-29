# 🏁 Sprint 1 – Importação de Bases, Motor Inicial e Chat

**🎯 Meta da Sprint:** Entregar o alicerce do sistema construindo a interface web para importar e cruzar as três bases de dados da Dom Rock (RH, Vendas e Comissionamento), processar as regras matemáticas básicas e proporcionais, e disponibilizar a interface inicial de chat com a IA para consultas.

**📊 Capacidade Estimada:** 42 Story Points

---

### 🔹 User Stories e Critérios de Aceitação (BDD / Gherkin)

#### 1. Importação de Planilhas (Front-end / Back-end)
* **User Story (#1):** Como Administrador de Regras, quero uma interface web para importar as planilhas mensais de RH, Vendas e Comissões, para que o sistema tenha os dados exatos para calcular o mês de competência.
* **Estimativa:** 8 Story Points
* **Critérios de Aceitação:**
  * **Cenário 1: Importação de bases completa e bem-sucedida**
    * **Dado que:** o Administrador de Regras acessa a tela de "Importar Bases"; e anexa as planilhas válidas de "RH", "Vendas" e "Comissionamento" referentes ao mês atual.
    * **Quando:** ele clicar no botão "Processar Bases".
    * **Então:** o sistema deve carregar os dados; relacionar a Base de Vendas com a Base de RH pela matrícula e código da loja; e exibir uma mensagem de sucesso na tela.
  * **Cenário 2: Tentativa de importação com arquivo ausente**
    * **Dado que:** o Administrador de Regras acessa a tela de "Importar Bases"; e anexa apenas as planilhas de "RH" e "Vendas" (esquecendo a de Comissionamento).
    * **Quando:** ele clicar no botão "Processar Bases".
    * **Então:** o sistema NÃO deve realizar o processamento; e deve exibir um alerta bloqueante informando "A Base de Comissionamento é obrigatória para o cálculo".

---

#### 2. Interface de Chat (Front-end)
* **User Story (#2):** Como Analista de Operações, quero conversar com o assistente por meio de uma interface de chat, para realizar perguntas diretas sobre os valores de comissionamento de um funcionário específico.
* **Estimativa:** 8 Story Points
* **Critérios de Aceitação:**
  * **Cenário 1: Consulta de valores de um funcionário válido**
    * **Dado que:** o fechamento da folha do mês já foi processado pelo motor; e o Analista está na tela do Assistente de Chat.
    * **Quando:** ele digitar a pergunta "Qual o valor da comissão do funcionário matrícula MATRIC-102?".
    * **Então:** a interface de chat deve processar a mensagem; e retornar o valor total exato calculado para aquela matrícula no mês.
  * **Cenário 2: Consulta de matrícula inexistente**
    * **Dado que:** o Analista está na tela do Assistente de Chat.
    * **Quando:** ele digitar uma pergunta sobre uma matrícula inexistente na base de RH (ex: "MATRIC-9999").
    * **Então:** o assistente deve responder informando que "O funcionário informado não foi encontrado nas bases do mês de competência atual".

---

#### 3. Cálculo de Comissões Básicas (Motor de Regras)
* **User Story (#3):** Como Analista de Operações, quero que o assistente calcule as comissões básicas (vendas individuais para vendedores e total de loja para gerentes), para agilizar o fechamento inicial da folha de pagamento.
* **Estimativa:** 13 Story Points
* **Critérios de Aceitação:**
  * **Cenário 1: Cálculo de comissão para Vendedor**
    * **Dado que:** o funcionário analisado possui um código de cargo de Vendedor (ex: 100, 200 ou 300); e possui R$ 10.000 em vendas individuais cadastradas na base.
    * **Quando:** o sistema aplicar a regra de cálculo daquele mês.
    * **Então:** o sistema deve aplicar o % de comissionamento da marca estritamente sobre as vendas individuais (R$ 10.000) do funcionário.
  * **Cenário 2: Cálculo de comissão para Gerente**
    * **Dado que:** o funcionário analisado possui o código de cargo de Gerente de Loja (150); e a soma total das vendas de todos os vendedores da sua loja resulta em R$ 150.000.
    * **Quando:** o sistema aplicar a regra de cálculo daquele mês.
    * **Então:** o sistema deve aplicar o % de comissionamento do Gerente sobre o valor total de vendas da loja (R$ 150.000), ignorando suas vendas individuais.

---

#### 4. Proporcionalidades de RH (Motor de Regras)
* **User Story (#4):** Como Analista de Operações, quero que o sistema calcule proporcionalmente as comissões de funcionários com férias, admissão ou demissão no mês, para evitar pagamentos indevidos.
* **Estimativa:** 13 Story Points
* **Critérios de Aceitação:**
  * **Cenário 1: Cálculo com Admissão ou Demissão no mês**
    * **Dado que:** um funcionário foi admitido (ou demitido) no dia 10 do mês de novembro (mês de 30 dias).
    * **Quando:** o sistema calcular sua comissão final.
    * **Então:** o motor deve pegar o valor integral da comissão apurada, dividir por 30 (dias do mês); e multiplicar por 21 dias (dias efetivamente trabalhados de 10 a 30).
  * **Cenário 2: Desconto de dias de Férias**
    * **Dado que:** um funcionário saiu de férias do dia 10/07 até o dia 25/07.
    * **Quando:** o sistema calcular a comissão de julho.
    * **Então:** o motor deve identificar que os 16 dias de férias não dão direito a remuneração; e realizar o cálculo proporcional pagando apenas os dias de trabalho efetivo.
