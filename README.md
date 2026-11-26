# UniMart - Feira de Trocas

Aplicação desktop Java Swing com SQLite para feira de trocas e doações da comunidade Senac.

O projeto está separado em dois módulos Maven:

- `backend/`: modelos, controlador, regras de negócio e acesso ao banco de dados.
- `frontend/`: janela, componentes Swing, diálogos e ponto de entrada da aplicação.

## Como executar

Na raiz do repositório, execute:

```bash
mvn -pl frontend -am install
mvn -pl frontend exec:java
```

Também é possível abrir a pasta raiz no VS Code, aguardar a importação dos módulos Maven e executar `frontend/src/main/java/br/com/unimart/Main.java`.
