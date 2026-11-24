# UniMart — feira de trocas e doações

Projeto simples para a comunidade do Senac, organizado em três partes:

- `docs/`: documentação e decisões do projeto.
- `frontend/`: interface feita com HTML, CSS e JavaScript puro.
- `backend/`: servidor e API feitos em Java 21, sem Spring Boot.
- `database/`: arquivo SQLite criado automaticamente na primeira execução.

## Tecnologias

- Front-end: HTML, CSS e JavaScript (sem React e sem Tailwind).
- Back-end: Java 21 com `HttpServer` nativo (sem Spring Boot).
- Banco de dados: SQLite.
- Maven: usado apenas para compilar e baixar as bibliotecas necessárias (SQLite JDBC e Gson).

## Como executar

1. Instale o Java 21 e o Maven.
2. Abra a pasta `backend` no terminal:
   ```bash
   cd backend
   ```
3. Inicie o servidor:
   ```bash
   mvn clean compile exec:java
   ```
4. Abra no navegador: http://localhost:8080

O banco será criado em `database/feira_trocas.db`, na raiz do projeto.

## Funcionalidades incluídas

- Publicar anúncios com título, descrição, categoria, modalidade, nome, turma e link de imagem.
- Listar anúncios salvos no SQLite.
- Buscar por palavra no título ou descrição.
- Filtrar por categoria e modalidade.
- Enviar mensagens de interesse, salvas no banco.
- Atualizar status de anúncio pela API.

Consulte `docs/README.pt-BR.md` para mais detalhes e `docs/DECISIONS.pt-BR.md` para as decisões técnicas.
