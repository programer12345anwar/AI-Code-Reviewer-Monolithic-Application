package com.anwar.aicodereview.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresMigrationTest {

    @Test
    void postgresqlMigrationCreatesExpectedTablesAndIndexes() throws Exception {
        String url = "jdbc:h2:mem:postgres_migration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration/postgresql")
                .load();

        assertEquals(1, flyway.migrate().migrationsExecuted);
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var tables = connection.getMetaData().getTables(null, "PUBLIC", "%", new String[]{"TABLE"})) {
            long tableCount = 0;
            while (tables.next()) {
                if (tables.getString("TABLE_NAME").startsWith("CODE_")
                        || tables.getString("TABLE_NAME").equals("USER_ACCOUNT")) {
                    tableCount++;
                }
            }
            assertEquals(3, tableCount);
        }
    }
}
