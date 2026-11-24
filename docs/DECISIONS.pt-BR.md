# Decisões técnicas

[English](DECISIONS.md)

- Front-end em HTML, CSS e JavaScript puro para reduzir dependências e facilitar o entendimento do código.
- Back-end em Java 21 usando `com.sun.net.httpserver.HttpServer`, evitando framework web.
- SQLite para manter os dados em um arquivo local, sem configurar servidor de banco.
- Maven é usado somente para gerenciar as bibliotecas JDBC do SQLite e Gson.
- A aplicação serve os arquivos do front-end e a API no mesmo endereço, simplificando a execução local.
- O escopo inicial prioriza publicação, listagem, busca, filtros e registro de interesse.
