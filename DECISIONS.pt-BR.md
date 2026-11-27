# Decisões do projeto

[English](DECISIONS.md) · **Português (Brasil)**

Este arquivo registra as decisões refletidas na implementação atual.

## 23/11/2026 — Stack Java para desktop

- **Decisão:** Usar Java 21, Swing e Maven.
- **Alternativas consideradas:** Uma aplicação web com JavaScript ou outra
  linguagem no backend.
- **Motivo:** A equipe já trabalha principalmente com Java, e uma aplicação
  desktop é adequada ao MVP de uma semana.

## 26/11/2026 — Atualização para a versão LTS mais recente do Java

- **Decisão:** Atualizar o runtime e o alvo de build do projeto para Java 25,
  mantendo Swing e Maven.
- **Motivo:** Java 25 é a versão LTS mais recente; o alvo do compilador Maven,
  o runtime do CI e os requisitos documentados agora usam essa versão.

## 24/11/2026 — Separação dos módulos frontend e backend

- **Decisão:** Manter a interface Swing em `frontend/` e os modelos, serviços,
  controller e persistência em `backend/`.
- **Alternativa considerada:** Manter todo o código da aplicação em um módulo.
- **Motivo:** O Maven mantém os módulos integrados e facilita localizar suas
  responsabilidades.

## 25/11/2026 — Implementação atual do banco de dados

- **Decisão refletida no código:** Usar SQLite, com um arquivo de banco local
  criado durante a execução.
- **Alternativa considerada:** Um banco Supabase compartilhado.
- **Motivo e situação:** O Supabase foi registrado como proposta em uma versão
  anterior das decisões, mas esta implementação não contém integração nem
  credenciais do Supabase. Um banco hospedado compartilhado permanece fora do
  escopo.

## 26/11/2026 — Links de fotos

- **Decisão:** A foto é opcional. Quando informada, deve usar um link público
  HTTP(S); arquivos de imagem não são enviados.
- **Alternativa considerada:** Armazenar arquivos de imagem junto ao anúncio.
- **Motivo:** A aplicação atual exibe imagens a partir de URLs e não possui um
  serviço de armazenamento de imagens. Manter a foto opcional permite publicar
  mesmo sem um link de imagem.

## 26/11/2026 — Descrições de anúncios com IA

- **Decisão:** Adiar a integração de um provedor de IA.
- **Alternativa considerada:** Enviar o texto dos anúncios a uma API de IA
  hospedada.
- **Motivo:** Nenhum provedor ou chave de API foi escolhido. Os dados dos
  anúncios não devem ser enviados a um serviço externo sem uma decisão explícita.

## 27/11/2026 — Apoio local à busca e descrição

- **Decisão:** Implementar uma sugestão local de descrição e reconhecimento
  simples de termos de busca, sem enviar dados a provedores externos.
- **Motivo:** Mantém o uso de IA transparente e seguro no prazo da imersão,
  permitindo uma demonstração de assistência sem exigir uma chave de API.

## 27/11/2026 — Integridade de interesses e denúncias

- **Decisão:** Impedir interesses duplicados, permitir cancelamento pelo
  interessado e permitir uma denúncia por usuário e anúncio somente quando o
  anúncio pertence a outra pessoa.
- **Motivo:** Fecha casos importantes de uso, reduz ruído no mural e cria
  responsabilidades claras para demonstrar interesse ou reportar conteúdo. A
  interface oculta a ação nos anúncios do próprio usuário e o backend também
  rejeita denúncias do proprietário.
