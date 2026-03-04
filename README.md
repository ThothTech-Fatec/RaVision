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

## 📃 Backlog do Produto

| Rank | Prioridade | User Story | Estimativa | Sprint | Status |
|------|------------|------------|------------|--------|--------|
| 1 | Alta | Como Administrador de Regras, quero cadastrar uma nova regra de negócio para formalizar decisões operacionais. | 5 | 1 | ⏳ |
| 2 | Alta | Como Administrador de Regras, quero editar e versionar regras para manter histórico de alterações. | 8 | 1 | ⏳ |
| 3 | Alta | Como Analista de Operações, quero simular a aplicação de uma regra para entender seu impacto antes de executá-la. | 8 | 1 | ⏳ |
| 4 | Alta | Como Analista de Operações, quero que o sistema explique automaticamente a decisão utilizando IA Generativa para garantir transparência. | 13 | 2 | ⏳ |
| 5 | Alta | Como Auditor Interno, quero visualizar o histórico de decisões aplicadas para garantir rastreabilidade. | 8 | 2 | ⏳ |
| 6 | Média | Como Gestor Comercial, quero simular cenários alternativos alterando parâmetros da regra para avaliar impacto financeiro. | 13 | 3 | ⏳ |
| 7 | Média | Como Administrador de Regras, quero que o sistema identifique possíveis conflitos entre regras cadastradas para evitar inconsistências. | 13 | 3 | ⏳ |
| 8 | Média | Como Administrador de Regras, quero que a IA sugira melhorias na redação da regra para torná-la mais clara. | 8 | 3 | ⏳ |
| 9 | Média | Como Auditor Interno, quero exportar relatório explicativo em PDF das decisões para documentação formal. | 5 | 4 | ⏳ |
| 10 | Baixa | Como Gestor Comercial, quero receber alertas quando regras forem alteradas para acompanhar mudanças estratégicas. | 5 | 4 | ⏳ |

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
