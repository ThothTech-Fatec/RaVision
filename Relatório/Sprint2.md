🏁 Sprint 2 – Governança de Regras, Auditoria e Cálculos Avançados

🎯 Meta da Sprint: Evoluir o sistema com controle de acesso, criação inteligente de regras via IA, fluxo de auditoria e aplicação de cenários avançados de cálculo (multi-loja, afastamentos e bônus), além de explicações detalhadas dos resultados.

📊 Capacidade Estimada: 79 Story Points

🔹 User Stories e Critérios de Aceitação (BDD / Gherkin)


5. Controle de Acesso e Perfis de Usuário
User Story (#5): Como Administrador do Sistema, quero estabelecer níveis de acesso no projeto, para garantir segurança e controle, assegurando que regras sejam auditadas antes de serem aplicadas.
Estimativa: 8 Story Points
Critérios de Aceitação:
Cenário 1: Acesso restrito por perfil
Dado que: existem perfis cadastrados (Administrador, Auditor, Analista).
Quando: um usuário tentar acessar funcionalidades restritas.
Então: o sistema deve permitir ou bloquear o acesso conforme seu perfil.
Cenário 2: Restrição de publicação de regras
Dado que: uma nova regra foi criada.
Quando: ela ainda não foi auditada.
Então: o sistema deve impedir sua ativação até aprovação.


6. Criação e Edição de Regras via Chat com IA
User Story (#6): Como Administrador de Regras, quero criar e editar regras conversando com a IA, para tornar o processo mais ágil e intuitivo.
Estimativa: 13 Story Points
Critérios de Aceitação:
Cenário 1: Criação de regra por linguagem natural
Dado que: o administrador está no chat.
Quando: ele descrever uma regra (ex: "adicionar bônus fixo para o funcionário de matrícula 120").
Então: o sistema deve interpretar e estruturar a regra corretamente.
Cenário 2: Edição de regra existente
Dado que: já existe uma regra cadastrada.
Quando: o administrador solicitar alteração via chat.
Então: o sistema deve atualizar a regra mantendo histórico.


7. Fluxo de Auditoria de Regras
User Story (#7): Como Gestor de RH, quero revisar regras geradas pela IA antes da aplicação, garantindo controle e conformidade.
Estimativa: 8 Story Points
Critérios de Aceitação:
Cenário 1: Regra criada com status pendente
Dado que: uma nova regra foi gerada pela IA.
Quando: ela for salva.
Então: deve ficar com status "Pendente".
Cenário 2: Aprovação ou reprovação
Dado que: o gestor está revisando regras.
Quando: ele aprovar ou rejeitar.
Então: o sistema deve atualizar o status e registrar a decisão.


8. Identificação de Funcionários em Múltiplas Lojas
User Story (#8): Como Analista, quero identificar funcionários que trabalharam em mais de uma loja no mês, para cálculo proporcional correto.
Estimativa: 8 Story Points
Critérios de Aceitação:
Cenário 1: Funcionário com múltiplas lojas
Dado que: um funcionário possui registros em mais de uma loja.
Quando: o cálculo for executado.
Então: o sistema deve dividir a comissão proporcionalmente por período ou volume de vendas.
Cenário 2: Funcionário com uma única loja
Dado que: o funcionário trabalhou em apenas uma loja.
Quando: o cálculo for executado.
Então: o sistema deve aplicar a regra padrão sem divisão.


9. Regras de Afastamento Médico
User Story (#9): Como Analista, quero aplicar regras de afastamento médico, garantindo conformidade com legislação.
Estimativa: 13 Story Points
Critérios de Aceitação:
Cenário 1: Afastamento menor que 15 dias
Dado que: o funcionário ficou afastado por menos de 15 dias.
Quando: o cálculo for realizado.
Então: o sistema deve pagar normalmente com proporcionalidade.
Cenário 2: Afastamento maior que 15 dias
Dado que: o afastamento ultrapassa 15 dias.
Quando: o cálculo for executado.
Então: o sistema deve considerar o piso de R$ 3.500 conforme regra.


10. Aplicação de Bônus e Incentivos
User Story (#10): Como Gestor de RH, quero aplicar bônus sazonais e incentivos, para motivar a equipe.
Estimativa: 8 Story Points
Critérios de Aceitação:
Cenário 1: Bônus fixo
Dado que: existe um bônus fixo definido para o período.
Quando: o cálculo for executado.
Então: o sistema deve adicionar o valor fixo ao total da comissão do funcionário.
Cenário 2: Bônus por campanhas (ex: Black Friday)
Dado que: existe campanha ativa.
Quando: o funcionário atingir metas.
Então: o sistema deve aplicar o bônus adicional.


11. Explicação Detalhada dos Cálculos
User Story (#11): Como Analista, quero entender como o valor foi calculado, para justificar resultados.
Estimativa: 13 Story Points
Critérios de Aceitação:
Cenário 1: Explicação passo a passo
Dado que: o cálculo foi realizado.
Quando: o usuário solicitar explicação.
Então: o sistema deve detalhar cada etapa do cálculo.
Cenário 2: Explicação de valores complexos
Dado que: existem múltiplas regras aplicadas.
Quando: a explicação for exibida.
Então: o sistema deve apresentar os cálculos de forma clara e organizada.


12. Histórico de Conversas e Alterações de Regras
User Story (#12): Como Administrador de Regras, quero visualizar o histórico de conversas com a IA e o registro de alterações nas regras, para ter controle e acompanhamento das ações realizadas no sistema.
Estimativa: 8 Story Points
Critérios de Aceitação:
Cenário 1: Histórico de conversas com a IA
Dado que: o administrador já utilizou o chat da IA.
Quando: ele acessar a tela de histórico.
Então: o sistema deve exibir as conversas realizadas com data e conteúdo das mensagens.
Cenário 2: Registro de criação de regras
Dado que: uma nova regra foi criada.
Quando: o histórico for acessado.
Então: o sistema deve exibir a data da criação e o conteúdo da regra.
Cenário 3: Registro de edição de regras
Dado que: uma regra foi alterada.
Quando: o histórico for consultado.
Então: o sistema deve exibir a data da alteração e o conteúdo atualizado da regra.
Cenário 4: Registro de exclusão de regras
Dado que: uma regra foi excluída.
Quando: o histórico for acessado.
Então: o sistema deve exibir a data da exclusão e a identificação da regra removida.
