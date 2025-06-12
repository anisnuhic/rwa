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

    private static volatile Connection connection = null;
    private static final Object lock = new Object();

    // Glavna metoda za konekciju
    public static Connection getConnection() {
        if (connection == null) {
            synchronized (lock) {
                if (connection == null) { // Double-check locking
                    initializeDatabase();
                }
            }
        }
        
        // Provjeri da li je konekcija još uvijek validna
        try {
            if (connection != null && (connection.isClosed() || !connection.isValid(2))) {
                System.out.println("⚠️ Konekcija nije validna, recreating...");
                connection = null;
                initializeDatabase();
            }
        } catch (SQLException e) {
            System.err.println("Greška pri provjeri konekcije: " + e.getMessage());
            connection = null;
            initializeDatabase();
        }

        return connection;
    }

    private static void initializeDatabase() {
        try {
            System.out.println("🔄 Inicijalizujem bazu podataka...");
            
            // Registruj driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL driver učitan");

            // Prvo se poveži bez baze da bi se mogla kreirati
            try (Connection initialConnection = DriverManager.getConnection(URL_WITHOUT_DB, USERNAME, PASSWORD);
                 Statement stmt = initialConnection.createStatement()) {

                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                System.out.println("✅ Baza '" + DB_NAME + "' kreirana/provjerena");
            }

            // Sada se poveži na konkretnu bazu
            connection = DriverManager.getConnection(URL_WITH_DB, USERNAME, PASSWORD);
            System.out.println("✅ Povezano na bazu '" + DB_NAME + "'");

            // Kreiraj tabele
            createTablesIfNotExist();
            
            // Dodaj početne podatke ako je potrebno
            insertInitialDataIfEmpty();

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL driver nije pronađen: " + e.getMessage());
            connection = null;
        } catch (SQLException e) {
            System.err.println("❌ Greška pri konekciji sa bazom: " + e.getMessage());
            e.printStackTrace();
            connection = null;
        }
    }

    private static void createTablesIfNotExist() throws SQLException {
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

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Tabela 'videos' kreirana/provjerena");
        }
    }

    private static void insertInitialDataIfEmpty() throws SQLException {
        // Provjeri da li tabela ima podatke
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM videos");
            rs.next();
            int count = rs.getInt(1);
            
            if (count == 0) {
                System.out.println("📦 Dodavam početne videe...");
                
                String[] insertStatements = {
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Rick Astley - Never Gonna Give You Up', 'dQw4w9WgXcQ', 'https://www.youtube.com/embed/dQw4w9WgXcQ')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('PSY - GANGNAM STYLE', '9bZkp7q19f0', 'https://www.youtube.com/embed/9bZkp7q19f0')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Luis Fonsi - Despacito ft. Daddy Yankee', 'kJQP7kiw5Fk', 'https://www.youtube.com/embed/kJQP7kiw5Fk')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Ed Sheeran - Shape of You', 'JGwWNGJdvx8', 'https://www.youtube.com/embed/JGwWNGJdvx8')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Queen - Bohemian Rhapsody', 'fJ9rUzIMcZQ', 'https://www.youtube.com/embed/fJ9rUzIMcZQ')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Alan Walker - Faded', '60ItHLz5WEA', 'https://www.youtube.com/embed/60ItHLz5WEA')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('ADELE - Hello', 'ZZ5LpwO-An4', 'https://www.youtube.com/embed/ZZ5LpwO-An4')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('OneRepublic - Counting Stars', 'hT_nvWreIhg', 'https://www.youtube.com/embed/hT_nvWreIhg')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('The Chainsmokers - Closer ft. Halsey', 'L_jWHffIx5E', 'https://www.youtube.com/embed/L_jWHffIx5E')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Imagine Dragons - Thunder', 'djV11Xbc914', 'https://www.youtube.com/embed/djV11Xbc914')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('The Weeknd - Blinding Lights', '4NRXx6U8ABQ', 'https://www.youtube.com/embed/4NRXx6U8ABQ')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Dua Lipa - Levitating', 'TUVcZfQe-Kw', 'https://www.youtube.com/embed/TUVcZfQe-Kw')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Olivia Rodrigo - drivers license', 'ZmDBbnmKpqQ', 'https://www.youtube.com/embed/ZmDBbnmKpqQ')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Justin Bieber - Peaches', 'tQ0yjYUFKAE', 'https://www.youtube.com/embed/tQ0yjYUFKAE')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Billie Eilish - bad guy', 'DyDfgMOUjCI', 'https://www.youtube.com/embed/DyDfgMOUjCI')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Post Malone - Circles', 'wXhTHyIgQ_U', 'https://www.youtube.com/embed/wXhTHyIgQ_U')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Michael Jackson - Billie Jean', 'Zi_XLOBDo_Y', 'https://www.youtube.com/embed/Zi_XLOBDo_Y')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Eagles - Hotel California', 'BciS5krYL80', 'https://www.youtube.com/embed/BciS5krYL80')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Led Zeppelin - Stairway to Heaven', 'QkF3oxziUI4', 'https://www.youtube.com/embed/QkF3oxziUI4')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Maroon 5 - Sugar', '09R8_2nJtjg', 'https://www.youtube.com/embed/09R8_2nJtjg')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Ariana Grande - 7 rings', 'QYh6mYIJG2Y', 'https://www.youtube.com/embed/QYh6mYIJG2Y')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Bruno Mars - Uptown Funk', 'OPf0YbXqDm0', 'https://www.youtube.com/embed/OPf0YbXqDm0')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Coldplay - Viva La Vida', 'dvgZkm1xWPE', 'https://www.youtube.com/embed/dvgZkm1xWPE')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Taylor Swift - Shake It Off', 'nfWlot6h_JM', 'https://www.youtube.com/embed/nfWlot6h_JM')",
                    "INSERT INTO videos (title, youtube_id, embed_url) VALUES ('Eminem - Lose Yourself', '_Yhyp-_hX2s', 'https://www.youtube.com/embed/_Yhyp-_hX2s')"
                };
                
                for (String sql : insertStatements) {
                    stmt.execute(sql);
                }
                
                System.out.println("✅ Dodano " + insertStatements.length + " početnih videa!");
            } else {
                System.out.println("ℹ️ Baza već sadrži " + count + " videa");
            }
        }
    }

    // Metoda za zatvaranje konekcije
    public static void closeConnection() {
        synchronized (lock) {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                    System.out.println("✅ Konekcija zatvorena");
                } catch (SQLException e) {
                    System.err.println("❌ Greška pri zatvaranju konekcije: " + e.getMessage());
                }
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