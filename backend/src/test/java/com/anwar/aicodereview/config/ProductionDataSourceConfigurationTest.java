package com.anwar.aicodereview.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionDataSourceConfigurationTest {

    @Test
    void acceptsPostgresJdbcUrlAndSeparateCredentials() {
        var details = ProductionDataSourceConfiguration.parseConnectionDetails(
                "jdbc:postgresql://db.example:5432/reviewer",
                "app_user",
                "app_password");

        assertEquals("jdbc:postgresql://db.example:5432/reviewer", details.jdbcUrl());
        assertEquals("app_user", details.username());
        assertEquals("app_password", details.password());
    }

    @Test
    void convertsRenderPostgresUriAndReadsEncodedCredentials() {
        var details = ProductionDataSourceConfiguration.parseConnectionDetails(
                "postgres://url_user:p%40ss%2Bword@db.example:5432/reviewer?sslmode=require",
                "",
                "");

        assertEquals(
                "jdbc:postgresql://db.example:5432/reviewer?sslmode=require",
                details.jdbcUrl());
        assertEquals("url_user", details.username());
        assertEquals("p@ss+word", details.password());
    }

    @Test
    void separateCredentialsOverrideCredentialsInConnectionUri() {
        var details = ProductionDataSourceConfiguration.parseConnectionDetails(
                "postgresql://url_user:url_password@db.example:5432/reviewer",
                "configured_user",
                "configured_password");

        assertEquals("configured_user", details.username());
        assertEquals("configured_password", details.password());
    }

    @Test
    void rejectsUnsupportedOrMalformedDatabaseUrlsWithoutEchoingThem() {
        String secretUrl = "mysql://private-user:private-password@db.example/reviewer";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ProductionDataSourceConfiguration.parseConnectionDetails(
                        secretUrl, "", ""));

        assertTrue(exception.getMessage().contains("DB_URL"));
        assertTrue(!exception.getMessage().contains("private-password"));
    }
}
