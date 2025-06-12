package com.youtube.voting.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_NAME = "youtube_voting";
    private static final String URL_WITHOUT_DB = "jdbc:mysql://localhost:3306/";
    private static final String URL_WITH_DB = "jdbc:mysql://localhost:3306/" + DB_NAME + "?serverTimezone=UTC";
    private static final String USERNAME = "anis"; // Promijeni po potrebi
    private static final String PASSWORD = "anis"; // Promijeni po potrebi

    private static Connection connection = null;

    // Glavna metoda za konekciju
    public static Connection getConnection() {
        try {
            // Ako veza ne postoji ili nije validna, napravi je
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Prvo se poveži bez baze da bi se mogla kreirati
                try (Connection initialConnection = DriverManager.getConnection(URL_WITHOUT_DB, USERNAME, PASSWORD);
                     Statement stmt = initialConnection.createStatement()) {

                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                    System.out.println("Baza '" + DB_NAME + "' je osigurana.");
                }

                // Sada se poveži na konkretnu bazu
                connection = DriverManager.getConnection(URL_WITH_DB, USERNAME, PASSWORD);
                System.out.println("Povezano na bazu '" + DB_NAME + "'.");

                // Napravi tabelu videos ako ne postoji
                createVideosTable(connection);
            }

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver nije pronađen: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Greška pri konekciji sa bazom: " + e.getMessage());
            connection = null;
        }

        return connection;
    }

    // Metoda za pravljenje tabele videos
    private static void createVideosTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS videos (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "title VARCHAR(255) NOT NULL, " +
            "youtube_id VARCHAR(100) NOT NULL UNIQUE, " +
            "embed_url VARCHAR(255), " +
            "positive_votes INT DEFAULT 0, " +
            "negative_votes INT DEFAULT 0, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "wilsonScore DOUBLE DEFAULT 0.0" +
            ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Tabela 'videos' je osigurana.");
        }
    }

    // Metoda za zatvaranje konekcije
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Konekcija zatvorena.");
            } catch (SQLException e) {
                System.err.println("Greška pri zatvaranju konekcije: " + e.getMessage());
            }
        }
    }

    // Test metoda
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
