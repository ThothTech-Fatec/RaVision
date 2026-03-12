<img width="1920" height="720" alt="Banner do Projeto" src="" />

# 🧠 Ra Vision – Gerenciamento Inteligente de Regras de Negócio

## 🔍 Visão Geral
Desenvolvido por nós, alunos do 6º semestre de ADS da Fatec São José dos Campos – Jessen Vidal, o **Ra Vision** é uma aplicação web para gerenciamento inteligente de regras de negócio com suporte de **IA Generativa**.

O sistema permite cadastrar, versionar, simular e explicar regras empresariais, promovendo maior rastreabilidade, transparência e redução de inconsistências operacionais.

Parceiro Acadêmico: **Dom Rock**

---

## 🎯 Desafio do Semestre
Empresas trabalham com regras de negócio dinâmicas que sofrem alterações constantes devido a campanhas, mudanças estratégicas e acordos comerciais.  

Muitas dessas regras:
- Não estão formalmente documentadas  
- Geram conflitos operacionais  
- Não possuem rastreabilidade  
- São de conhecimento tácito  

Nosso objetivo é desenvolver uma aplicação que organize essas regras e utilize **IA Generativa** para explicar decisões e apoiar a tomada de decisão empresarial.

---

## 🚀 Solução Proposta
A aplicação oferecerá:

- Cadastro e versionamento de regras de negócio  
- Simulação de aplicação de regras  
- Explicação automática de decisões via LLM  
- Identificação de possíveis conflitos  
- Histórico e rastreabilidade das decisões  
- Geração de relatórios explicativos  

---

## 🧩 MVP (Produto Mínimo Viável)

O MVP contempla:

- Cadastro de regras
- Listagem de regras
- Simulação de aplicação
- Explicação automática da decisão
- Histórico de execução

---

## 👥 Personas do Sistema

- **Administrador de Regras** – Responsável por cadastrar e manter regras.
- **Analista de Operações** – Aplica regras e precisa entender decisões.
- **Gestor Comercial** – Avalia impacto financeiro das regras.
- **Auditor Interno** – Necessita rastreabilidade e justificativas formais.

---

#### 📃 Backlog do Produto
| Rank | Prioridade | User Story | Estimativa | Sprint | Status |
| ------ | ------ | ------ | ------ | ------ | ------ |
| 1 | Alta | **[Front-end]** Como Administrador de Regras, quero uma interface web SPA em Vue.js, para cadastrar e listar normativas da empresa de forma centralizada e sem planilhas soltas. | 8 | 1 | ⏳ |
| 2 | Alta | **[Front-end]** Como Analista de Operações, quero uma interface de Chat interativa, para agilizar a obtenção de respostas dinâmicas sobre regras de comissionamento vigentes. | 8 | 1 | ⏳ |
| 3 | Alta | **[Back-end]** Como Administrador de Regras, quero que os dados base do negócio (vendas, funcionários e comissionamento) sejam estruturados via SpringBoot, para permitir a integração e execução segura das regras cadastradas. | 13 | 1 | ⏳ |
| 4 | Alta | **[Machine Learning]** Como Administrador de Regras, quero que os documentos físicos e PDFs da empresa sejam fatiados e convertidos em formato vetorial, para que a Inteligência Artificial consiga ler o conhecimento tácito da Dom Rock. | 13 | 1 | ⏳ |
| 5 | Alta | **[Machine Learning]** Como Administrador de Regras, quero que as informações vetorizadas sejam indexadas em um banco de dados inteligente, para garantir que as buscas de contexto do sistema sejam ágeis e precisas. | 8 | 1 | ⏳ |
| 6 | Alta | **[Machine Learning]** Como Analista de Operações, quero que o contexto exato da minha pergunta seja recuperado no banco de dados vetorial, para que a resposta da IA seja baseada apenas na nossa realidade e não contenha alucinações. | 13 | 2 | ⏳ |
| 7 | Alta | **[Machine Learning]** Como Analista de Operações, quero enviar simulações para um motor de Inteligência Artificial (LLM), para reduzir drasticamente meu tempo gasto decifrando regras complexas de descontos e campanhas. | 13 | 2 | ⏳ |
| 8 | Alta | **[Back-end]** Como Auditor Interno, quero que a aplicação registre logs estruturados e versionamento automático, para manter o histórico inalterável de todas as chamadas feitas à IA e garantir conformidade. | 8 | 2 | ⏳ |
| 9 | Média | **[Front-end]** Como Gestor Comercial, quero um painel visual para simular cenários operacionais, para avaliar preventivamente o impacto financeiro de uma nova comissão antes de publicá-la. | 8 | 2 | ⏳ |
| 10 | Média | **[Back-end]** Como Analista de Operações, quero que as rotas da aplicação se comuniquem perfeitamente com os scripts em Python, para garantir fluidez e evitar travamentos durante o expediente. | 8 | 2 | ⏳ |
| 11 | Alta | **[Machine Learning]** Como Analista de Operações, quero que a IA justifique detalhadamente como calculou a comissão (Explicabilidade/XAI), para garantir a transparência da decisão e evitar discussões com a equipe de vendas. | 13 | 3 | ⏳ |
| 12 | Média | **[Back-end]** Como Administrador de Regras, quero que o sistema me alerte sobre conflitos entre regras recém-cadastradas e antigas, para evitar pagamentos indevidos e inconsistências operacionais. | 8 | 3 | ⏳ |
| 13 | Alta | **[Todas as Áreas]** Como Analista de Operações, quero consultar um Manual do Usuário detalhado, para reduzir minha curva de aprendizado ao começar a utilizar o assistente de IA da Dom Rock. | 5 | 3 | ⏳ |
| 14 | Alta | **[Todas as Áreas]** Como CTO da Dom Rock, quero acessar um Manual de Instalação completo no repositório, para garantir que minha equipe interna consiga implantar o projeto no servidor sem depender dos alunos. | 5 | 3 | ⏳ |
| 15 | Alta | **[Front-end / Apoio]** Como Gestor Comercial, quero assistir a um Vídeo Tutorial demonstrativo da ferramenta, para repassar aos diretores e funcionários que não possuem conhecimento técnico aprofundado. | 5 | 3 | ⏳ |

---

# ✅ Critérios de Aceitação Detalhados

 ### <a href="./Relatório/Sprint1.md">1️⃣ Critérios referentes à 1ª Sprint </a> 
 
---

#### 🚦 Definition of Ready (DoR)
O DoR representa o nosso acordo de equipe indicando quando uma *User Story* está preparada para ter seu desenvolvimento iniciado em uma Sprint. Uma tarefa só será puxada para desenvolvimento se cumprir os seguintes critérios de checklist:

**Sobre a User Story:**
- [ ] Possui título claro, descrição bem definida e objetivo compreendido por todos.
- [ ] Os Critérios de Aceitação estão detalhados e documentados.
- [ ] As regras de negócio da tarefa estão claras.
- [ ] A história foi estimada pela equipe (em *Story Points*).
- [ ] A tarefa não possui dependências bloqueadoras que impeçam seu início.
- [ ] A compreensão da entrega foi validada com todo o time.

**Sobre os Artefatos Correlatos:**
- [ ] Design/documentação visual (Wireframes/Mockups) estão disponíveis (para tarefas de Front-end).
- [ ] O Modelo de Dados está definido e disponível (para tarefas de Back-end).
- [ ] A estratégia de testes foi definida.

---

#### 🏁 Definition of Done (DoD)
O DoD é o checklist de compromisso da nossa equipe que garante que a *User Story* foi totalmente concluída e atinge os critérios de qualidade necessários para ser entregue à Dom Rock.

**Critérios de Conclusão:**
- [ ] O código foi devidamente versionado no Git.
- [ ] O desenvolvimento ocorreu em uma branch específica (`feature/` ou `bugfix/`).
- [ ] Foi aberto um *Pull Request* (PR) antes da liberação para *Code Review*.
- [ ] Fragmentos de código comentados ou lixo de desenvolvimento foram removidos.
- [ ] Todos os Critérios de Aceitação da *User Story* foram plenamente satisfeitos e testados.
- [ ] Testes de unidade foram implementados com cobertura de código mínima de 70%.
- [ ] A funcionalidade roda localmente na máquina sem falhas e sem quebrar o que já existia.

---

## 📈 Requisitos Funcionais

- CRUD completo de regras de negócio  
- Versionamento de regras  
- Simulação de aplicação  
- Explicação automatizada via LLM  
- Registro de histórico de execuções  
- Geração de relatórios  

---

## 📊 Requisitos Não Funcionais

- **SpringBoot** para Backend  
- **Vue.js (SPA)** para Frontend  
- Integração com **Modelos LLM via API pública**  
- Utilização de framework como **LangChain / LangGraph / LlamaIndex**  
- Manual de Instalação  
- Manual do Usuário  
- Vídeo tutorial demonstrativo  
- Segurança e controle de acesso  

---

## 🛠️ Tecnologias

### Backend
- SpringBoot  
- Java  
- PostgreSQL  

### Frontend
- Vue.js (SPA)  
- TypeScript   

### IA Generativa
- Integração com API LLM (OpenAI / Gemini / Hugging Face)  
- Framework de orquestração (LangChain ou similar)  

### DevOps
- Docker  
- GitHub   

---

## 🌿 Estratégia de Branch

Utilizaremos o modelo **GitHub Flow**:

- `main` → versão estável  
- `feature/nome-da-feature`  
- `bugfix/nome-do-bug`  

Todo desenvolvimento ocorre em branch separada com abertura obrigatória de **Pull Request** antes do merge.

---

## 📝 Padrão de Commits

Seguiremos o padrão simplificado de Conventional Commits:
