# Organização do projeto

## docs/
Contém README, decisões técnicas e registro do uso de ferramentas de IA.

## frontend/
- `index.html`: estrutura da página.
- `style.css`: estilos e responsividade.
- `app.js`: interações da interface e chamadas à API.

## backend/
- `src/main/java/br/com/unimart/Main.java`: inicialização do servidor HTTP, rotas da API, acesso ao SQLite e entrega dos arquivos do front-end.
- `pom.xml`: dependências e configuração de compilação Java 21.

## database/
O arquivo `feira_trocas.db` é criado automaticamente quando o servidor inicia pela primeira vez.
