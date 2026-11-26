# UniMart — Feira de Trocas do Campus

[English](README.md) · **Português (Brasil)**

O UniMart é um aplicativo desktop para estudantes trocarem ou doarem livros,
materiais escolares e equipamentos usados dentro da comunidade Senac.

## Equipe

- **Turma:** TDS — turmas mistas 6570, 6756 e 6863
- **Tech lead:** Rodrigo Genevro
- **Responsável pela apresentação:** Warley Dos Reis Neto
- **Integrantes:** Rodrigo Genevro, Vinicius Brilhante, Warley Dos Reis Neto e
  Gabryel Pedroso de Menezes Santos

## Problema e solução

Estudantes precisam de materiais que outras pessoas já não usam, mas as ofertas
ficam espalhadas por canais informais. O UniMart reúne anúncios, busca de itens
e mensagens de interesse em um só lugar.

## Tecnologias

- Java 21
- Interface desktop com Java Swing
- Banco de dados SQLite local
- Build Maven com módulos (`backend/` e `frontend/`)

## Funcionalidades atuais

- Cadastro, login e edição de perfil
- Publicação de anúncios com título, descrição, categoria, modalidade de
  troca/doação e link público opcional de foto HTTP(S)
- Busca e filtros de anúncios disponíveis
- Envio de mensagens de interesse em anúncios disponíveis
- Controles do anunciante para ver interesses, aceitar um deles e reservar o
  anúncio, reabrir uma reserva ou concluir o anúncio

O banco de dados é local à máquina; não há banco compartilhado hospedado.
A geração de descrições com IA foi adiada; o aplicativo não envia dados dos
anúncios a um provedor externo de IA.

## Estrutura do repositório

```text
/
├── README.md
├── README.pt-BR.md
├── DECISIONS.md
├── DECISIONS.pt-BR.md
├── AI_LOG.md
├── AI_LOG.pt-BR.md
├── backend/              # Modelos, controller, serviços e repositórios
├── frontend/             # Telas Swing, componentes e entrada da aplicação
├── config/               # Regras do Checkstyle
├── database/             # Banco SQLite criado durante a execução
├── .github/workflows/    # Pipeline de build, testes e lint
└── docs/                 # Briefing, regras e notas de arquitetura
```

## Requisitos

- JDK 21
- Maven 3.9 ou superior

## Como executar

Na raiz do repositório, instale os módulos e inicie a interface Swing:

```bash
mvn -pl frontend -am install
mvn -pl frontend exec:java
```

O banco SQLite é criado automaticamente em `database/feira_trocas.db`.

## Build, testes e lint

```bash
mvn --batch-mode --no-transfer-progress verify
```

O comando executa os testes do backend e do frontend e o Checkstyle. O GitHub
Actions executa essa validação em todo pull request e em cada push para `main`.

## Deploy e links

- **Repositório:** https://github.com/RodrigoGenevro/Feira-de-trocas
- **Deploy:** Não configurado; o aplicativo é desktop.
- **Board do GitHub:** Não configurado.
