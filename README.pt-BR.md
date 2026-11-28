# Trocaê — Feira de Trocas do Campus

[English](README.md) · **Português (Brasil)**

O Trocaê é um aplicativo desktop para estudantes trocarem ou doarem livros,
materiais escolares e equipamentos usados dentro da comunidade Senac.

## Equipe

- **Turma:** TDS — turmas mistas 6570, 6756 e 6863
- **Tech lead:** Rodrigo Genevro
- **Responsável pela apresentação:** Warley Dos Reis Neto
- **Integrantes:** Rodrigo Genevro, Vinicius Brilhante, Warley Dos Reis Neto e
  Gabryel Pedroso de Menezes Santos

## Problema e solução

Estudantes precisam de materiais que outras pessoas já não usam, mas as ofertas
ficam espalhadas por canais informais. O Trocaê reúne anúncios, busca de itens
e mensagens de interesse em um só lugar.

## Tecnologias

- Java 21
- Interface desktop com Java Swing
- SQLite em arquivo local, sem serviço externo
- Build Maven com módulos (`backend/` e `frontend/`)

## Funcionalidades atuais

- Cadastro, login e edição de perfil
- Publicação de anúncios com título, descrição, categoria, modalidade de
  troca/doação e link público opcional de foto HTTP(S)
- Busca e filtros de anúncios disponíveis
- Resultados paginados e ordenáveis, com controles acessíveis pelo teclado
- Validação do formulário e prévia assíncrona da foto, com aviso quando ela não
  puder ser carregada
- Cabeçalho responsivo em duas linhas mantém visíveis as ações de conta e
  publicação
- Envio de mensagens de interesse em anúncios disponíveis
- Controles do anunciante para ver interesses, aceitar um deles e reservar o
  anúncio, reabrir uma reserva, concluir ou reativar o anúncio
- Edição de anúncios disponíveis pelo próprio anunciante em **Meus anúncios**
- Grade responsiva de anúncios, filtros acessíveis e chips removíveis para os
  filtros ativos
- Migrações versionadas do SQLite, integridade referencial e
  índices para busca de anúncios
- Interesses enviados podem ser cancelados pelo próprio usuário
- Prevenção de interesses duplicados no backend
- Denúncia de anúncios de outras pessoas com motivo e detalhes; não é possível
  denunciar o próprio anúncio
- Sugestão local de descrição e busca em linguagem natural

O aplicativo acessa diretamente um banco SQLite local em
`database/feira_trocas.db`. O arquivo é criado e atualizado automaticamente na
primeira execução. Os dados ficam somente nesse computador; não há API, servidor
ou sincronização entre instalações. O arquivo `.env` não é necessário nem usado.
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
├── backend/              # modelos, serviços, controller, repositórios e SQLite
├── frontend/             # Telas Swing, componentes e entrada da aplicação
├── config/               # Regras do Checkstyle
├── database/             # Arquivo SQLite local (ignorado pelo Git)
├── .github/workflows/    # Pipeline de build, testes e lint
└── docs/                 # Briefing, regras e notas de arquitetura
```

## Requisitos

- JDK 21 ou superior
- Maven 3.9 ou superior (ou use o Maven Wrapper incluído)

## Como executar

Na raiz do repositório, compile e execute o desktop:

```powershell
.\mvnw.cmd clean install
.\mvnw.cmd -pl frontend exec:java
```

Na primeira execução, o aplicativo cria a pasta `database/`, o arquivo
`feira_trocas.db` e aplica as migrações automaticamente. Para fazer backup,
use **Banco de dados → Fazer backup** e escolha onde salvar o arquivo. Para
recuperar os dados, use **Banco de dados → Restaurar backup** e selecione um
backup válido; a restauração substitui os dados atuais. Não versionamos arquivos
de banco no Git.

## Build, testes e lint

```bash
mvn --batch-mode --no-transfer-progress verify
```

O comando executa os testes do backend e do frontend e o Checkstyle. Os testes
usam bancos SQLite temporários, sem acessar dados pessoais. O GitHub Actions executa
esse comando no Ubuntu com Temurin JDK 25 em todo pull request e em cada push
para `main`. O workflow usa `actions/checkout@v7` e
`actions/setup-java@v6`, mantendo ativas as anotações de erros do compilador
Java; consulte [o workflow de CI](.github/workflows/ci.yml).

## Links

- **Repositório:** https://github.com/RodrigoGenevro/Feira-de-trocas
- **Board do GitHub:** Não configurado.
