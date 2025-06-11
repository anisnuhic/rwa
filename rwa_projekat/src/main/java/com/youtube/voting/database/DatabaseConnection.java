package com.youtube.voting.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/youtube_voting";
    private static final String USERNAME = "anis"; // Promijeni prema tvojim podacima
    private static final String PASSWORD = "anis"; // Promijeni prema tvojim podacima
    
    private static Connection connection = null;
    
    // Singleton pattern za konekciju
    public static Connection getConnection() {
        try {
            // Provjeri da li je konekcija zatvorena ili nevažeća
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                // Registruj MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Stvori konekciju
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Uspješno povezano sa bazom podataka!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver nije pronađen: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Greška pri konekciji sa bazom: " + e.getMessage());
            connection = null;
        }
        
        return connection;
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