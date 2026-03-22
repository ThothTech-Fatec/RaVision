#### 🏁 Sprint 1 – Fundação, Interface e Ingestão de Dados (RAG)
**Capacidade estimada da Equipe:** 50 Story Points
**Meta da Sprint:** Construir a base da aplicação web (Front-end Vue.js e Back-end Spring Boot) e implementar a primeira fase da IA, garantindo que os documentos físicos da Dom Rock sejam vetorizados no banco de dados.

##### 🔹 User Stories e Critérios de Aceitação

###### 1. CRUD de Regras (Front-end)
*   **User Story:** Como Administrador de Regras, quero uma interface web SPA em Vue.js, para cadastrar e listar normativas da empresa de forma centralizada e sem planilhas soltas.
*   **Critérios:**
    1. A interface deve permitir o preenchimento de título, descrição e validade da regra.
    2. O sistema deve exibir uma tabela com todas as regras já cadastradas e seus respectivos status.
    3. Campos obrigatórios em branco devem disparar um alerta visual na tela.

---

###### 2. Interface de Chat (Front-end)
*   **User Story:** Como Analista de Operações, quero uma interface de Chat interativa, para agilizar a obtenção de respostas dinâmicas sobre regras de comissionamento vigentes.
*   **Critérios:**
    1. A tela deve conter um campo de entrada de texto e um botão de envio (Submit).
    2. O histórico do chat (perguntas e respostas) deve ser exibido de forma sequencial na tela.
    3. Deve existir um indicador visual (ex: *spinner*) enquanto a IA processa a resposta.

---

###### 3. Estruturação do Banco e API (Back-end)
*   **User Story:** Como Administrador de Regras, quero que os dados base do negócio (vendas, funcionários e comissionamento) sejam estruturados via SpringBoot, para permitir a integração e execução segura das regras cadastradas.
*   **Critérios:**
    1. O banco de dados PostgreSQL deve estar instanciado com as tabelas de Vendas, Funcionários e Regras.
    2. As rotas RESTful (GET, POST, PUT, DELETE) para as regras de negócio devem estar operacionais e testadas no Postman/Swagger.

---

###### 4. Ingestão e Vetorização de Documentos (Machine Learning)
*   **User Story:** Como Administrador de Regras, quero que os documentos físicos e PDFs da empresa sejam fatiados e convertidos em formato vetorial, para que a Inteligência Artificial consiga ler o conhecimento tácito da Dom Rock.
*   **Critérios:**
    1. O script Python deve ser capaz de ler e extrair texto de arquivos (PDFs/Documentos da Dom Rock).
    2. O texto extraído deve ser particionado (Text Chunks) utilizando o framework LangChain ou Llama Index.
    3. Os Chunks devem ser submetidos a um Embedding Model e convertidos em vetores matemáticos.

---

###### 5. Criação do Banco Vetorial (Machine Learning)
*   **User Story:** Como Administrador de Regras, quero que as informações vetorizadas sejam indexadas em um banco de dados inteligente, para garantir que as buscas de contexto do sistema sejam ágeis e precisas.
*   **Critérios:**
    1. O Banco de Dados Vetorial deve ser configurado e acessível.
    2. O script deve gravar os vetores de texto gerados e criar um índice (Vector Index) com sucesso.
