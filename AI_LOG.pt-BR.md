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
