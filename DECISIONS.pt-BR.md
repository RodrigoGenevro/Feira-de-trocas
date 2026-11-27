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

## 25/11/2026 — Primeira implementação do banco de dados

- **Decisão refletida no código:** Usar SQLite, com um arquivo de banco local
  criado durante a execução.
- **Alternativa considerada:** Um banco Supabase compartilhado.
- **Motivo e situação:** Esta foi a implementação inicial; a decisão foi
  substituída em 27/11/2026 pela integração compartilhada descrita abaixo.

## 27/11/2026 — Banco compartilhado no Supabase

- **Decisão:** Manter o cliente desktop Swing, adicionar uma API Java hospedada
  e usar PostgreSQL no Supabase como armazenamento compartilhado.
- **Alternativa considerada:** Conectar cada instalação desktop diretamente ao
  banco remoto.
- **Motivo:** Uma API centralizada mantém as credenciais do banco fora do
  aplicativo distribuído e aplica autenticação e autorização no servidor.
- **Operação:** Configurar URL JDBC, segredo de sessão e URL pública da API por
  variáveis de ambiente; o deploy público exige HTTPS.

## 26/11/2026 — Links de fotos e requisito do briefing

- **Implementação atual:** O link da foto é opcional. Quando informado, deve
  usar HTTP(S); arquivos de imagem não são enviados.
- **Requisito do briefing:** O fluxo mínimo de publicação exige uma foto.
- **Situação de conformidade:** O comportamento atual, que permite anúncio sem
  foto, não atende a esse requisito. A implementação e esta decisão precisam
  ser revisadas antes de declarar conformidade integral com o briefing.
- **Alternativa considerada:** Armazenar arquivos de imagem junto ao anúncio.
- **Motivo do comportamento atual:** A aplicação exibe imagens a partir de URLs
  e não possui um serviço de armazenamento de imagens. Este registro descreve
  o comportamento existente; não dispensa o requisito do briefing.

## 26/11/2026 — Descrições de anúncios com IA

- **Decisão:** Adiar a integração de um provedor de IA.
- **Alternativa considerada:** Enviar o texto dos anúncios a uma API de IA
  hospedada.
- **Motivo:** Nenhum provedor ou chave de API foi escolhido. Os dados dos
  anúncios não devem ser enviados a um serviço externo sem uma decisão explícita.
