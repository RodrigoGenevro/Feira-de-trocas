# UniMart — versão simples

Projeto de feira de trocas e doações para a comunidade do Senac.

## Tecnologias
- HTML, CSS e JavaScript puro no front-end.
- Java 21 com `HttpServer` nativo no back-end (sem Spring Boot).
- SQLite para persistência.
- Maven apenas para baixar as bibliotecas SQLite JDBC e Gson.

## Como executar
1. Instale Java 21 e Maven.
2. Abra a pasta do projeto no VS Code.
3. No terminal, entre na pasta `backend`:
   ```bash
   cd backend
   mvn clean compile exec:java
   ```
4. Acesse http://localhost:8080.

O banco é criado automaticamente em `database/feira_trocas.db`.

## O que já funciona
- Publicar anúncio com título, descrição, categoria, modalidade, nome e turma.
- Listar anúncios salvos no SQLite.
- Buscar por palavra no título ou descrição.
- Filtrar por categoria e modalidade.
- Enviar mensagem de interesse registrada no banco.

## Próximos passos do MVP
- Tela para o anunciante consultar interesses recebidos.
- Ações para reservar e concluir anúncio.
- Upload de foto para o servidor (atualmente o formulário aceita um link de imagem).
- Validação mais completa e regras de autorização.

## Estrutura
```text
docs/
frontend/index.html
frontend/style.css
frontend/app.js
backend/src/main/java/br/com/unimart/Main.java
backend/pom.xml
database/feira_trocas.db (criado ao executar)
```
