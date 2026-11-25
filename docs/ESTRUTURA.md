# Organização do Projeto (Arquitetura MVC + Service)

## backend/src/main/java/br/com/unimart/
- `Main.java`: Ponto de entrada da aplicação, inicializa o banco e carrega a interface.
- `model/`:
  - `EntidadeBase.java`: Superclasse abstrata com encapsulamento de ID, status e data de criação.
  - `Anuncio.java`: Modelo de domínio dos itens anunciados (herda de `EntidadeBase`).
  - `Interesse.java`: Modelo de domínio das manifestações de interesse (herda de `EntidadeBase`).
- `repository/`:
  - `AnuncioRepository.java`: Persistência SQLite para anúncios.
  - `InteresseRepository.java`: Persistência SQLite para interesses.
- `service/`:
  - `AnuncioService.java`: Regras de negócio e filtros de anúncios.
  - `InteresseService.java`: Regras de negócio e validações de interesses.
- `controller/`:
  - `FeiraController.java`: Mediação entre as telas (View) e os serviços (Service).
- `view/`:
  - `MainFrame.java`: Janela principal da aplicação Swing.
  - `components/AnuncioCard.java`: Componente visual de card de item.
  - `dialogs/BaseDialog.java`: Superclasse abstrata para modais Swing.
  - `dialogs/NovoAnuncioDialog.java`: Modal de publicação de anúncio (herda de `BaseDialog`).
  - `dialogs/InteresseDialog.java`: Modal de envio de interesse (herda de `BaseDialog`).
- `util/`:
  - `DatabaseConfig.java`: Configuração e inicialização da conexão SQLite.
  - `Theme.java`: Identidade visual, cores e componentes estilizados.

## database/
O arquivo `feira_trocas.db` é criado automaticamente quando a aplicação inicia pela primeira vez.
