# Organização do projeto

O Maven integra dois módulos. O backend fornece modelos, regras de negócio,
controller, repositórios e persistência SQLite; o frontend fornece a interface
desktop Swing. O aplicativo conecta diretamente ao arquivo
`database/feira_trocas.db`; não há API HTTP ou banco remoto.

## `backend/src/main/java/br/com/unimart/`

- `model/`: entidades `Anuncio`, `Denuncia`, `Interesse`, `Usuario` e
  `EntidadeBase`.
- `repository/`: consultas e persistência de anúncios, denúncias, interesses e
  usuários usando SQLite.
- `service/`: regras de negócio e validações de anúncios, denúncias, interesses
  e autenticação.
- `controller/`: coordena as operações da aplicação com os serviços.
- `util/`: configuração, migrações e backup do banco SQLite, além de utilitários
  de senha.

## `frontend/src/main/java/br/com/unimart/`

- `Main.java`: inicializa o banco SQLite local e inicia o fluxo de autenticação
  e a janela principal.
- `view/MainFrame.java`: tela principal, busca, filtros e anúncios disponíveis.
- `view/components/`: componentes Swing reutilizáveis, incluindo os cards de anúncios.
- `view/dialogs/`: publicação, perfil, login, envio e gestão de interesses e histórico.
- `util/Theme.java`: cores e componentes visuais compartilhados.

## Outros diretórios

- `database/`: arquivo SQLite local, criado na primeira execução e ignorado
  pelo Git.
- `config/`: regras Checkstyle usadas pelo build.
- `.github/workflows/`: pipeline de build, testes e lint.
- `docs/`: briefing, regras da atividade e esta descrição da arquitetura.

O arquivo do banco permanece no computador onde o aplicativo é executado.
Instalações em computadores diferentes mantêm dados separados; para preservar
os dados, feche o aplicativo e faça backup de `database/feira_trocas.db`.
