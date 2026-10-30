package com.example.paypulse.config;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RefreshDatabase {
    private static final String DATABASE_NAME = env("SPRING_DATASOURCE_NAME", "paypulse_db");
    private static final String USER = env("SPRING_DATASOURCE_USERNAME", "postgres");
    private static final String PASSWORD = env("SPRING_DATASOURCE_PASSWORD", "");
    private static final String DATABASE_URL = env("SPRING_DATASOURCE_URL",
            "jdbc:postgresql://localhost:5432/" + DATABASE_NAME);
    private static final String SERVER_URL = serverUrl(DATABASE_URL);

    public static void main(String[] args) {
        try {
            createDatabaseIfMissing();
            recreatePublicSchema();
            runMigrations();
            System.out.println("DB succesfully refreshed!");

        } catch (Exception e) {
            System.err.println("Error at refreshing DB:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void createDatabaseIfMissing() throws SQLException {
        try (Connection conn = DriverManager.getConnection(SERVER_URL + "/postgres", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + DATABASE_NAME + "'");
            if (!rs.next()) {
                System.out.println("DB " + DATABASE_NAME + " doesn't exist, I'm creating it...");
                stmt.executeUpdate("CREATE DATABASE " + DATABASE_NAME);
            }
        }
    }

    private static void recreatePublicSchema() throws SQLException {
        System.out.println("Connecting to: " + DATABASE_URL);
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        }
    }

    private static void runMigrations() {
        Flyway.configure()
                .dataSource(DATABASE_URL, USER, PASSWORD)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    private static String serverUrl(String jdbcUrl) {
        int queryStart = jdbcUrl.indexOf('?');
        String withoutQuery = queryStart >= 0 ? jdbcUrl.substring(0, queryStart) : jdbcUrl;
        int lastSlash = withoutQuery.lastIndexOf('/');
        return lastSlash > "jdbc:postgresql:".length() ? withoutQuery.substring(0, lastSlash) : withoutQuery;
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
