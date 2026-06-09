# 🏁 Sprint 3 – Monitoramento, Analytics e Qualidade de Dados

## 🎯 Meta da Sprint

Evoluir a plataforma com recursos de detecção de anomalias, dashboards gerenciais, monitoramento operacional do assistente e validação de dados importados, garantindo maior confiabilidade, governança e visibilidade dos processos de comissionamento.

## 📊 Capacidade Estimada

**34 Story Points**

---

# 🔹 User Stories e Critérios de Aceitação (BDD / Gherkin)

## 13. Identificação de Anomalias no Comissionamento

**User Story (#13):** Como Gestor de RH, quero que o sistema identifique anomalias no cálculo de comissionamento (ex: desvios relevantes ou vendas muito acima da média), para analisar inconsistências e possíveis fraudes antes do fechamento da folha.

**Estimativa:** 8 Story Points

### Critérios de Aceitação

#### Cenário 1: Identificação de venda acima da média

**Dado que:** existem históricos de vendas dos funcionários.  
**Quando:** o sistema processar os cálculos de comissionamento.  
**Então:** deve identificar funcionários com vendas significativamente acima da média definida pela empresa.

#### Cenário 2: Identificação de divergência de comissão

**Dado que:** existe uma comissão calculada fora do padrão esperado.  
**Quando:** o processo de validação for executado.  
**Então:** o sistema deve sinalizar a ocorrência para análise do gestor.

#### Cenário 3: Exibição de alertas

**Dado que:** foram encontradas anomalias.  
**Quando:** o gestor acessar os resultados do processamento.  
**Então:** o sistema deve exibir alertas contendo o motivo da sinalização e os dados relacionados.

---

## 14. Dashboard de Comissões por Loja e Marca

**User Story (#14):** Como Gestor de RH, quero visualizar painéis e gráficos com os totais de comissões pagas por loja e marca, para analisar rapidamente o impacto financeiro das campanhas do mês.

**Estimativa:** 13 Story Points

### Critérios de Aceitação

#### Cenário 1: Visualização por loja

**Dado que:** existem dados de comissionamento processados.  
**Quando:** o gestor acessar o dashboard.  
**Então:** o sistema deve apresentar os valores totais de comissão agrupados por loja.

#### Cenário 2: Visualização por marca

**Dado que:** existem dados de diferentes marcas cadastradas.  
**Quando:** o gestor acessar o dashboard.  
**Então:** o sistema deve apresentar os valores totais de comissão agrupados por marca.

#### Cenário 3: Filtro por período

**Dado que:** o gestor deseja analisar um período específico.  
**Quando:** ele selecionar um intervalo de datas.  
**Então:** o sistema deve atualizar os gráficos e indicadores conforme o período informado.

#### Cenário 4: Indicadores consolidados

**Dado que:** existem dados disponíveis para análise.  
**Quando:** o dashboard for carregado.  
**Então:** o sistema deve exibir indicadores consolidados, como total de comissões pagas, quantidade de funcionários comissionados e média de comissão por colaborador.

---

## 15. Monitoramento de Uso e Performance do Assistente

**User Story (#15):** Como CTO da Dom Rock, quero monitorar o tempo de resposta e a quantidade de perguntas feitas ao assistente, para garantir que a plataforma está estável e eficiente.

**Estimativa:** 5 Story Points

### Critérios de Aceitação

#### Cenário 1: Registro de tempo de resposta

**Dado que:** um usuário realiza uma pergunta ao assistente.  
**Quando:** a resposta for processada.  
**Então:** o sistema deve registrar o tempo total de atendimento da solicitação.

#### Cenário 2: Registro de volume de perguntas

**Dado que:** usuários utilizam o assistente.  
**Quando:** novas interações forem realizadas.  
**Então:** o sistema deve contabilizar a quantidade de perguntas processadas.

#### Cenário 3: Visualização de métricas

**Dado que:** existem dados de utilização registrados.  
**Quando:** o CTO acessar a área de monitoramento.  
**Então:** o sistema deve apresentar métricas de uso e desempenho do assistente.

---

## 16. Validação de Dados das Planilhas Importadas

**User Story (#16):** Como Administrador de Regras, quero ser alertado na interface se as planilhas importadas contiverem erros (ex: funcionário sem data de admissão), para corrigir os dados antes de gerar os pagamentos.

**Estimativa:** 8 Story Points

### Critérios de Aceitação

#### Cenário 1: Identificação de campos obrigatórios ausentes

**Dado que:** uma planilha foi importada.  
**Quando:** existirem registros sem informações obrigatórias.  
**Então:** o sistema deve identificar os registros inválidos e informar os campos ausentes.

#### Cenário 2: Bloqueio de processamento com erros críticos

**Dado que:** a planilha contém inconsistências críticas.  
**Quando:** o usuário tentar iniciar o cálculo das comissões.  
**Então:** o sistema deve impedir o processamento até que os erros sejam corrigidos.

#### Cenário 3: Exibição detalhada dos erros

**Dado que:** foram encontrados problemas na importação.  
**Quando:** o usuário visualizar os resultados da validação.  
**Então:** o sistema deve exibir uma lista detalhada contendo linha, campo afetado e descrição do erro.

#### Cenário 4: Processamento de registros válidos

**Dado que:** a planilha possui registros válidos e inválidos.  
**Quando:** a validação for concluída.  
**Então:** o sistema deve informar a quantidade de registros válidos, inválidos e o percentual de aproveitamento dos dados.

---

# 📈 Resumo da Sprint 3

| ID | User Story | Prioridade | Story Points |
|----|------------|------------|--------------|
| 13 | Identificação de Anomalias no Comissionamento | Média | 8 |
| 14 | Dashboard de Comissões por Loja e Marca | Média | 13 |
| 15 | Monitoramento de Uso e Performance do Assistente | Média | 5 |
| 16 | Validação de Dados das Planilhas Importadas | Baixa | 8 |
| **Total** |  |  | **34 SP** |

---

## 🚀 Entregas da Sprint

- Detecção automática de anomalias e possíveis fraudes.
- Dashboard gerencial com indicadores por loja e marca.
- Monitoramento operacional do assistente de IA.
- Validação e qualidade de dados das planilhas importadas.
- Alertas preventivos para evitar erros de processamento e pagamento.
