package com.example.paypulse.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class RefreshDatabase {
    public static void main(String[] args) {
        String urlBase = "jdbc:postgresql://localhost:5432/";
        String targetDb = "paypulse_db";
        String user = "postgres";
        String password = ""; // here goes your psql pass

        try {
            try (Connection conn = DriverManager.getConnection(urlBase + "postgres", user, password);
                 Statement stmt = conn.createStatement()) {

                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + targetDb + "'");
                if (!rs.next()) {
                    System.out.println("DB" + targetDb + " doesn't exist, I'm creating it...");
                    stmt.executeUpdate("CREATE DATABASE " + targetDb);
                }
            }

            System.out.println("Connecting to: " + urlBase + targetDb);
            try (Connection conn = DriverManager.getConnection(urlBase + targetDb, user, password);
                 Statement stmt = conn.createStatement()) {

                stmt.executeUpdate("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");

                InputStream is = RefreshDatabase.class.getClassLoader().getResourceAsStream("db.sql");
                if (is == null) {
                    is = RefreshDatabase.class.getClassLoader().getResourceAsStream("db/db.sql");
                }

                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                stmt.execute(sql);
                System.out.println("DB succesfully refreshed!");
            }

        } catch (Exception e) {
            System.err.println("Error at refreshing DB:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}