# Registro de uso de IA

[English](AI_LOG.md) · **Português (Brasil)**

Este registro documenta as ferramentas de IA usadas no projeto, prompts
representativos, o conteúdo gerado e as decisões tomadas após a revisão.

## Ferramentas

- ChatGPT
- Gemini
- GitHub Copilot

## Registros

### 24/11/2026 — Design inicial da tela

- **Prompt principal:** Usar o projeto existente da UC4 como base para a nova
  tela inicial.
- **O que a IA gerou:** Uma implementação inicial da tela usando padrões de
  orientação a objetos.
- **O que o time aproveitou:** A implementação proposta não foi usada.
- **O que o time corrigiu ou rejeitou, e por quê:** O design não atendia aos
  objetivos de apresentação, e problemas no GitHub impediram adotar aquela
  versão.

### 25/11/2026 — Melhoria do design das telas

- **Prompt principal:** Melhorar as telas da aplicação.
- **O que a IA gerou:** Uma reformulação visual com cores que diminuíam a
  visibilidade dos botões.
- **O que o time aproveitou:** A direção do layout.
- **O que o time corrigiu ou rejeitou, e por quê:** As cores foram ajustadas
  para manter os botões visíveis.

### 26/11/2026 — Conformidade do MVP e fluxos do anunciante

- **Prompt principal:** Comparar a aplicação com as regras e o briefing do
  projeto e implementar as lacunas identificadas sem alterar desnecessariamente
  a aparência existente.
- **O que a IA gerou:** Alterações de código e documentação que inicialmente
  exigiam links de foto, adicionavam a gestão de interesses e status pelo
  anunciante e incluíam testes, lint e CI.
- **O que o time aproveitou:** O anunciante pode revisar interesses recebidos,
  aceitar um para reservar o anúncio, reabrir uma reserva ou concluir o anúncio.
- **O que o time corrigiu ou rejeitou, e por quê:** A exigência de foto foi
  revertida porque a foto deve ser opcional. A geração de descrição por IA também
  foi adiada; nenhum conteúdo de anúncio é enviado a um provedor externo de IA.

### 26/11/2026 — Atualização para Java LTS e ajustes no fluxo de anúncios

- **Prompt principal:** Atualizar o runtime Java para a versão LTS mais recente
  e aprimorar o comportamento de propriedade dos anúncios, saída da conta e
  atualização de status.
- **O que a IA gerou:** Atualizou o alvo do compilador Maven, a versão do Java
  no CI e os requisitos em português e inglês para Java 25. Alterou o botão de
  histórico para “Meus anúncios”, bloqueou o envio de interesse nos próprios
  anúncios na interface e no banco, ocultou a janela principal enquanto o login
  de saída é exibido e atualizou os anúncios disponíveis após reserva,
  conclusão ou reabertura. Também adicionou testes focados na restrição de
  interesse do anunciante.
- **O que o time aproveitou:** Java 25 como alvo do projeto e os comportamentos
  solicitados para anúncios e saída da conta.
- **O que o time corrigiu ou rejeitou, e por quê:** O valor
  `permitirInteresse` passou a ser um campo do card após um erro de compilação
  indicar que a variável não estava no escopo do método que cria o botão. Os
  testes adicionados não foram executados porque a execução de comandos locais
  não foi autorizada.

### 26/11/2026 — Tela de pré-login Trocaê

- **Prompt principal:** Criar uma tela de pré-login inspirada na imagem de
  referência enviada, usando as cores existentes da aplicação e alterando o
  nome visível para “Trocaê”.
- **O que a IA gerou:** Reformulou o diálogo de login e cadastro como uma tela
  dividida em dois painéis, com área de boas-vindas, gradiente verde e
  preservação do login por e-mail e senha, seleção rápida de contas e criação
  de conta. Atualizou a identidade visual exibida na janela principal e em
  diálogos relacionados.
- **O que o time aproveitou:** A composição dividida entre boas-vindas e acesso,
  adaptada à paleta verde já usada na aplicação e à interface Swing.
- **O que o time corrigiu ou rejeitou, e por quê:** A imagem enviada serviu
  apenas como referência visual; a tela foi implementada com componentes Swing e
  formas Java2D, sem incorporar a imagem. Os testes não foram executados para
  esta alteração.

### 26/11/2026 — Melhorias de confiabilidade e backup

- **Prompt principal:** Implementar as melhorias sugeridas: validar os fluxos,
  padronizar a marca Trocaê, melhorar o feedback das operações, oferecer backup
  do banco e adicionar testes de fluxo.
- **O que a IA gerou:** Atualizou os READMEs bilíngues, adicionou uma ação de
  backup por snapshot do SQLite, estados de carregamento e erro, bloqueio contra
  envios repetidos durante operações assíncronas e testes com banco temporário
  para integridade do backup e fluxo de cadastro até conclusão do anúncio.
- **O que o time aproveitou:** Destino do backup escolhido pelo usuário e testes
  isolados do banco real da aplicação.
- **O que o time corrigiu ou rejeitou, e por quê:** Os 12 testes descobertos
  pelo VS Code passaram. A tarefa Maven `verify`/Checkstyle não foi executada
  porque a ferramenta de build Java não iniciou com o modelo configurado.

### 26/11/2026 — Edição de anúncios

- **Prompt principal:** Adicionar uma opção para cada usuário editar os próprios
  anúncios.
- **O que a IA gerou:** Adicionou a ação de edição em **Meus anúncios**,
  reutilizou o formulário de publicação para salvar as alterações e incluiu
  verificações de propriedade e disponibilidade na atualização do banco.
- **O que o time aproveitou:** O anunciante pode editar anúncios disponíveis;
  anúncios reservados ou concluídos não podem ser editados.
- **O que o time corrigiu ou rejeitou, e por quê:** A edição foi limitada a
  anúncios disponíveis para não alterar reservas em andamento nem o histórico
  de anúncios concluídos. Foram adicionados testes direcionados ao repositório.

### 26/11/2026 — Busca de anúncios e integridade do banco

- **Prompt principal:** Implementar as melhorias sugeridas de frontend e
  backend, incluindo prévia de foto, validação do formulário, ordenação,
  acessibilidade, migrações versionadas do SQLite, paginação, integridade e
  testes direcionados.
- **O que a IA gerou:** Adicionou prévias assíncronas com limites de download,
  validação de campos e contagem de caracteres, resultados paginados e
  ordenáveis, melhorias de teclado e contraste, migrações versionadas,
  conexões com chaves estrangeiras, gatilhos de integridade, índices e
  cabeçalho em duas linhas para manter visível a ação de publicação.
- **O que o time aproveitou:** A arquitetura desktop e SQLite local existente,
  migração retrocompatível do banco legado e testes com bancos temporários.
- **O que o time corrigiu ou rejeitou, e por quê:** Os downloads das prévias
  têm limites de tempo, tamanho e dimensões. Os 23 testes descobertos pelo VS
  Code passaram. O Maven não está disponível no PATH, então não foi possível
  executar o build Maven.
### 26/11/2026 — Correção da cor do botão “Seu anúncio”

- **Prompt principal:** Quando o anúncio é meu, a cor do botão fica estranha;
  deixar ela da mesma cor dos outros botões.
- **O que a IA gerou:** Em `AnuncioCard.java`, o botão desabilitado do próprio
  anúncio passou a usar um `BasicButtonUI` customizado que pinta o texto sempre
  em branco e mantém o fundo verde `Theme.GREEN`, ignorando o efeito de
  escurecimento que o Swing aplica a botões desabilitados.
- **O que o time aproveitou:** O botão “Seu anúncio” agora tem exatamente a
  mesma aparência dos outros botões, mantendo-se não clicável e com o tooltip
  explicativo.
- **O que o time corrigiu ou rejeitou, e por quê:** Nada; a compilação com
  `javac` (incluindo os models do backend) passou sem erros.
