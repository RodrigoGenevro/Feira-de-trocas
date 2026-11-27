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

- Java 25 (LTS mais recente)
- Interface desktop com Java Swing
- API Java e PostgreSQL hospedado no Supabase
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
  anúncio, reabrir uma reserva ou concluir o anúncio
- Edição de anúncios disponíveis pelo próprio anunciante em **Meus anúncios**
- Grade responsiva de anúncios, filtros acessíveis e chips removíveis para os
  filtros ativos
- API HTTP para autenticação, perfil, anúncios e interesses
- Migrações versionadas para PostgreSQL, integridade referencial e índices de
  busca
- Sessões assinadas com prazo de validade; senhas e credenciais do banco ficam
  no servidor

O app foi preparado para compartilhar dados pelo PostgreSQL do Supabase através
de uma API Java. Para efetivar o compartilhamento, a API precisa ser hospedada e
todas as instalações devem apontar para a mesma URL com `TROCAE_API_URL`. O
cliente desktop não se conecta diretamente ao banco nem contém credenciais do
Supabase. Backups remotos devem ser gerenciados no projeto Supabase.
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
├── backend/              # API, modelos, serviços, controller e repositórios
├── frontend/             # Telas Swing, componentes e entrada da aplicação
├── config/               # Regras do Checkstyle
├── database/             # Banco SQLite legado e local de desenvolvimento
├── .github/workflows/    # Pipeline de build, testes e lint
└── docs/                 # Briefing, regras e notas de arquitetura
```

## Requisitos

- JDK 25
- Maven 3.9 ou superior

## Como executar

Crie um projeto Supabase e configure a API com a URL JDBC PostgreSQL, uma senha
de sessão aleatória (mínimo de 32 bytes) e a porta fornecida pelo provedor de
hospedagem. Para conexão com o Render, use no painel Supabase **Connect →
Session pooler** e copie host, porta, usuário e banco informados ali. A API
aplica as migrações ao iniciar:

```powershell
$env:TROCAE_DATABASE_URL = "jdbc:postgresql://HOST_DO_POOLER:5432/postgres?user=USUARIO_DO_POOLER&password=SENHA_CODIFICADA&sslmode=require"
$env:TROCAE_AUTH_SECRET = "substitua-por-um-segredo-aleatorio-com-32-bytes-ou-mais"
$env:PORT = "8080"
mvn -pl backend exec:java -Dexec.mainClass="br.com.unimart.api.ApiServer"
```

O usuário do pooler pode incluir a referência do projeto. Use os valores exatos
mostrados pelo painel; caracteres especiais na senha da URL devem ser codificados.
O modo Session aceita conexões persistentes e prepared statements. A conta do
banco precisa ter permissão para criar as tabelas e índices das migrações.

Para desenvolvimento local, inicie a API e, em outro terminal, inicie o app.
Em produção, configure `TROCAE_API_URL` em cada instalação desktop para a URL
HTTPS pública da API (`https://SEU-SERVIDOR/api`):

```powershell
$env:TROCAE_API_URL = "http://localhost:8080/api"
mvn -pl frontend -am install
mvn -pl frontend exec:java
```

Para importar uma vez os dados de um SQLite existente, faça primeiro uma cópia
do arquivo e use um banco Supabase vazio:

```powershell
mvn -pl backend exec:java -Dexec.mainClass="br.com.unimart.util.SqliteToPostgresMigration" -Dexec.args="database/feira_trocas.db"
```

O importador preserva os IDs, valida as quantidades e cancela a operação se as
tabelas de destino já contiverem registros. Não execute novamente após uma
migração bem-sucedida.

Nunca coloque a senha do banco ou `TROCAE_AUTH_SECRET` no código, no cliente
desktop ou em arquivos versionados. Use segredos do provedor de hospedagem e
configure a API com HTTPS antes de disponibilizá-la publicamente.

## Publicar a API no Render

1. Crie o projeto Supabase e, no painel, abra **Connect → Session pooler**.
   Copie host, porta, usuário e banco exibidos. Monte a URL como
   `jdbc:postgresql://HOST_DO_POOLER:5432/postgres?user=USUARIO_DO_POOLER&password=SENHA_CODIFICADA&sslmode=require`;
   use os valores exatos do painel. O Session pooler é compatível com IPv4 e
   conexões persistentes; não use o Transaction pooler para esta API.
2. Na raiz do repositório, crie uma branch de trabalho, confira as alterações e
   envie essa branch (use um nome único):

   ```powershell
   git switch -c feature/my-change
   git status
   git add Dockerfile .dockerignore render.yaml README.md README.pt-BR.md backend frontend DECISIONS.md DECISIONS.pt-BR.md
   git commit -m "feat: prepare deployment via Render and Supabase"
   git push -u origin feature/my-change
   ```

   Abra um pull request com destino a `main`; aguarde a revisão e o CI verde
   antes de mesclar. Não envie commits diretamente para `main`. Não inclua
   arquivos `.env`, senhas ou URLs com credenciais no commit.
3. No Render, escolha **New → Blueprint**, conecte o repositório GitHub e
   selecione a branch `main`. O Render lê `render.yaml`, pede
   `TROCAE_DATABASE_URL` e gera `TROCAE_AUTH_SECRET`. Não defina `PORT`; o Render
   fornece essa variável.
4. Quando o deploy ficar saudável, abra `https://<servico>.onrender.com/health`;
   a resposta esperada é `{"status":"ok"}`. A API cria as tabelas ao iniciar.
5. Em cada computador cliente, configure
   `TROCAE_API_URL=https://<servico>.onrender.com/api` e reinicie o app. Todos
   devem usar exatamente a mesma URL.

O plano gratuito do Render pode suspender a API quando está ociosa; a primeira
requisição após a suspensão pode demorar. O cliente desktop tem timeout atual
de 20 segundos, então uma inicialização a frio pode excedê-lo; tente novamente
depois que o serviço iniciar. Esta configuração inicia um banco Supabase vazio
e não importa os arquivos SQLite locais.

## Build, testes e lint

```bash
mvn --batch-mode --no-transfer-progress verify
```

O comando executa os testes do backend e do frontend e o Checkstyle. Os testes
SQLite continuam cobrindo o modo legado; a conexão Supabase exige configuração
externa e deve ser validada no ambiente de deploy. O GitHub Actions executa a
validação local em todo pull request e em cada push para `main`.

## Deploy e links

- **Repositório:** https://github.com/RodrigoGenevro/Feira-de-trocas
- **Deploy:** Blueprint `render.yaml` preparado; o serviço só fica publicado
  depois de conectar o GitHub e configurar o Supabase no Render.
- **Board do GitHub:** Não configurado.
