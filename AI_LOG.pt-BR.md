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
