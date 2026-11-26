# Organização do projeto

O Maven integra dois módulos: o backend contém regras de negócio e persistência;
o frontend depende do backend e fornece a interface desktop Swing.

## `backend/src/main/java/br/com/unimart/`

- `model/`: entidades `Anuncio`, `Interesse`, `Usuario` e `EntidadeBase`.
- `repository/`: consultas e persistência SQLite para anúncios, interesses e usuários.
- `service/`: regras de negócio e validações de anúncios, interesses e autenticação.
- `controller/`: coordena a interface, os serviços e o usuário autenticado.
- `util/`: configuração do SQLite e utilitários de senha.

## `frontend/src/main/java/br/com/unimart/`

- `Main.java`: inicializa o banco e exibe a autenticação e a janela principal.
- `view/MainFrame.java`: tela principal, busca, filtros e anúncios disponíveis.
- `view/components/`: componentes Swing reutilizáveis, incluindo os cards de anúncios.
- `view/dialogs/`: publicação, perfil, login, envio e gestão de interesses e histórico.
- `util/Theme.java`: cores e componentes visuais compartilhados.

## Outros diretórios

- `database/`: o arquivo `feira_trocas.db` é criado automaticamente na primeira execução.
- `config/`: regras Checkstyle usadas pelo build.
- `.github/workflows/`: pipeline de build, testes e lint.
- `docs/`: briefing, regras da atividade e esta descrição da arquitetura.
