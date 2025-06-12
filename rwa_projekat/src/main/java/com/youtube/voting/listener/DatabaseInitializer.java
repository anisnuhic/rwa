package com.youtube.voting.listener;

import com.youtube.voting.database.DatabaseConnection;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DatabaseInitializer implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== APLIKACIJA SE POKRECE ===");
        System.out.println("Inicijalizujem bazu podataka...");
        
        try {
            // Forsiraj kreiranje konekcije (što će kreirati bazu i tabele)
            DatabaseConnection.getConnection();
            System.out.println("✅ Baza podataka uspješno inicijalizovana!");
            
        } catch (Exception e) {
            System.err.println("❌ GREŠKA pri inicijalizaciji baze: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== APLIKACIJA SPREMNA ===");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== APLIKACIJA SE GASI ===");
        System.out.println("Zatvaram konekciju sa bazom...");
        
        try {
            DatabaseConnection.closeConnection();
            System.out.println("✅ Konekcija zatvorena!");
        } catch (Exception e) {
            System.err.println("❌ Greška pri zatvaranju: " + e.getMessage());
        }
        
        System.out.println("=== APLIKACIJA UGAŠENA ===");
    }
}