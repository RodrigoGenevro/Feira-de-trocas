package br.com.unimart.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

    @Test
    void deveConectarAoSupabaseComSucesso() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            assertNotNull(connection, "A conexão com o banco não deve ser nula.");
            assertTrue(!connection.isClosed(), "A conexão deve estar aberta.");
        } catch (SQLException e) {
            // Nota: Se estiver a rodar testes sem o arquivo .env configurado com credenciais válidas do Supabase,
            // esta exceção será lançada. Garanta que o .env está preenchido corretamente.
            throw new RuntimeException("Falha ao conectar com o Supabase. Verifique as credenciais no arquivo .env", e);
        }
    }

    @Test
    void deveInicializarMigracoesNoSupabase() {
        try {
            DatabaseConfig.initDatabase();
            assertTrue(true, "As tabelas e migrações foram aplicadas com sucesso no PostgreSQL.");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar as migrações no Supabase.", e);
        }
    }
}