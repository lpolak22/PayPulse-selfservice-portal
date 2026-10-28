package com.example.paypulse.config;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RefreshDatabase {
    private static final String URL_BASE = "jdbc:postgresql://localhost:5432/";
    private static final String TARGET_DB = "paypulse_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = ""; // here goes your psql pass

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
        try (Connection conn = DriverManager.getConnection(URL_BASE + "postgres", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + TARGET_DB + "'");
            if (!rs.next()) {
                System.out.println("DB" + TARGET_DB + " doesn't exist, I'm creating it...");
                stmt.executeUpdate("CREATE DATABASE " + TARGET_DB);
            }
        }
    }

    private static void recreatePublicSchema() throws SQLException {
        System.out.println("Connecting to: " + URL_BASE + TARGET_DB);
        try (Connection conn = DriverManager.getConnection(URL_BASE + TARGET_DB, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        }
    }

    private static void runMigrations() {
        Flyway.configure()
                .dataSource(URL_BASE + TARGET_DB, USER, PASSWORD)
                .locations("classpath:db/migration", "classpath:db/seed")
                .load()
                .migrate();
    }
}
