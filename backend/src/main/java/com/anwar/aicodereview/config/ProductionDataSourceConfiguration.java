package com.anwar.aicodereview.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("prod")
public class ProductionDataSourceConfiguration {

    private static final String INVALID_URL_MESSAGE =
            "DB_URL must be a PostgreSQL JDBC URL or a postgres:// connection URI";

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(
            @Value("${DB_URL}") String databaseUrl,
            @Value("${DB_USERNAME:}") String configuredUsername,
            @Value("${DB_PASSWORD:}") String configuredPassword) {
        ConnectionDetails details = parseConnectionDetails(
                databaseUrl, configuredUsername, configuredPassword);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl(details.jdbcUrl());
        dataSource.setUsername(details.username());
        dataSource.setPassword(details.password());
        return dataSource;
    }

    static ConnectionDetails parseConnectionDetails(
            String databaseUrl, String configuredUsername, String configuredPassword) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalArgumentException(INVALID_URL_MESSAGE);
        }

        String value = databaseUrl.trim();
        if (value.startsWith("jdbc:postgresql://")) {
            return new ConnectionDetails(
                    value, configuredUsername, configuredPassword);
        }

        try {
            URI uri = URI.create(value);
            if (!("postgres".equals(uri.getScheme()) || "postgresql".equals(uri.getScheme()))
                    || uri.getHost() == null
                    || uri.getRawPath() == null
                    || uri.getRawPath().length() < 2
                    || uri.getFragment() != null) {
                throw new IllegalArgumentException(INVALID_URL_MESSAGE);
            }

            String rawAuthority = uri.getRawAuthority();
            int userInfoEnd = rawAuthority.lastIndexOf('@');
            String hostAndPort = userInfoEnd >= 0
                    ? rawAuthority.substring(userInfoEnd + 1)
                    : rawAuthority;
            String jdbcUrl = "jdbc:postgresql://" + hostAndPort + uri.getRawPath()
                    + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());

            String[] uriCredentials = parseUserInfo(uri.getRawUserInfo());
            String username = configuredUsername == null || configuredUsername.isBlank()
                    ? uriCredentials[0]
                    : configuredUsername;
            String password = configuredPassword == null || configuredPassword.isBlank()
                    ? uriCredentials[1]
                    : configuredPassword;

            return new ConnectionDetails(jdbcUrl, username, password);
        } catch (IllegalArgumentException exception) {
            if (INVALID_URL_MESSAGE.equals(exception.getMessage())) {
                throw exception;
            }
            throw new IllegalArgumentException(INVALID_URL_MESSAGE);
        }
    }

    private static String[] parseUserInfo(String rawUserInfo) {
        if (rawUserInfo == null) {
            return new String[]{"", ""};
        }

        int separator = rawUserInfo.indexOf(':');
        if (separator < 0) {
            return new String[]{decode(rawUserInfo), ""};
        }
        return new String[]{
                decode(rawUserInfo.substring(0, separator)),
                decode(rawUserInfo.substring(separator + 1))
        };
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    record ConnectionDetails(String jdbcUrl, String username, String password) {
    }
}
