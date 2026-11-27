# Organização do projeto

O Maven integra dois módulos. O backend fornece a API HTTP, regras de negócio e
persistência; o frontend fornece o cliente desktop Swing, que se comunica com a
API. Em produção, a API usa PostgreSQL no Supabase por meio de
`TROCAE_DATABASE_URL`. Sem essa variável, o backend usa o SQLite local para
desenvolvimento e testes.

## `backend/src/main/java/br/com/unimart/`

- `model/`: entidades `Anuncio`, `Interesse`, `Usuario` e `EntidadeBase`.
- `api/`: servidor HTTP, cliente da API para o desktop e serviço de tokens de
  autenticação.
- `repository/`: consultas e persistência de anúncios, interesses e usuários
  usando a conexão configurada.
- `service/`: regras de negócio e validações de anúncios, interesses e autenticação.
- `controller/`: coordena as operações da aplicação com os serviços.
- `util/`: configuração e migrações do banco, importação de SQLite para
  PostgreSQL, backup e utilitários de senha.

## `frontend/src/main/java/br/com/unimart/`

- `Main.java`: configura o cliente HTTP da API e inicia o fluxo de autenticação
  e a janela principal.
- `view/MainFrame.java`: tela principal, busca, filtros e anúncios disponíveis.
- `view/components/`: componentes Swing reutilizáveis, incluindo os cards de anúncios.
- `view/dialogs/`: publicação, perfil, login, envio e gestão de interesses e histórico.
- `util/Theme.java`: cores e componentes visuais compartilhados.

## Outros diretórios

- `database/`: banco SQLite local criado automaticamente quando
  `TROCAE_DATABASE_URL` não está definida; também pode conter dados legados para
  importação.
- `config/`: regras Checkstyle usadas pelo build.
- `.github/workflows/`: pipeline de build, testes e lint.
- `docs/`: briefing, regras da atividade e esta descrição da arquitetura.

O cliente desktop não se conecta diretamente ao Supabase. Para compartilhar
dados, todas as instalações devem usar a mesma URL pública da API em
`TROCAE_API_URL`; as credenciais do PostgreSQL e o segredo de autenticação ficam
na configuração do servidor.
