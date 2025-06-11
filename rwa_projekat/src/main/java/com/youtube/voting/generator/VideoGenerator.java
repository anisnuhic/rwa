package com.youtube.voting.generator;

import com.github.javafaker.Faker;
import java.sql.*;
import java.util.Random;

public class VideoGenerator {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/youtube_voting";
    private static final String DB_USER = "anis";
    private static final String DB_PASSWORD = "anis";
    private static final int BATCH_SIZE = 1000;
    
    // 10 stvarnih YouTube videa sa STVARNIM ID-jima za thumbnail
    private static final String[][] YOUTUBE_VIDEOS = {
        {"dQw4w9WgXcQ", "Rick Astley - Never Gonna Give You Up"},
        {"9bZkp7q19f0", "PSY - GANGNAM STYLE"},
        {"kJQP7kiw5Fk", "Luis Fonsi - Despacito ft. Daddy Yankee"},
        {"JGwWNGJdvx8", "Ed Sheeran - Shape of You"},
        {"fJ9rUzIMcZQ", "Queen - Bohemian Rhapsody"},
        {"60ItHLz5WEA", "Alan Walker - Faded"},
        {"ZZ5LpwO-An4", "ADELE - Hello"},
        {"hT_nvWreIhg", "OneRepublic - Counting Stars"},
        {"L_jWHffIx5E", "The Chainsmokers - Closer ft. Halsey"},
        {"djV11Xbc914", "Imagine Dragons - Thunder"}
    };
    
    public static void main(String[] args) {
        VideoGenerator generator = new VideoGenerator();
        
        System.out.println("YOUTUBE VOTING - VIDEO GENERATOR");
        System.out.println("===================================");
        
        int numberOfVideos = 50;
        
        try {
            System.out.print("Koliko videa zelis generirati (Enter za 50): ");
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            String input = scanner.nextLine().trim();
            
            if (!input.isEmpty()) {
                numberOfVideos = Integer.parseInt(input);
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Koristim default: 50 videa");
            numberOfVideos = 50;
        }
        
        System.out.println("Cilj: " + numberOfVideos + " videa");
        System.out.println("Koristi 10 stvarnih YouTube videa (rotacija)");
        System.out.println("Generiše nazive sa Java Faker bibliotekom");
        System.out.println();
        
        try {
            if (!generator.testConnection()) {
                System.err.println("GRESKA: Ne mogu se povezati sa bazom podataka!");
                System.err.println("Provjeri da li je MySQL pokrenut i da su podaci za konekciju ispravni");
                return;
            }
            
            System.out.print("Obrisati postojece videe? (y/N): ");
            try {
                int ch = System.in.read();
                if (ch == 'y' || ch == 'Y') {
                    generator.clearVideos();
                }
            } catch (Exception e) {
                System.out.println("Nastavljam bez brisanja...");
            }
            
            long startTime = System.currentTimeMillis();
            boolean success = generator.generateVideos(numberOfVideos);
            long endTime = System.currentTimeMillis();
            double seconds = (endTime - startTime) / 1000.0;
            
            if (success) {
                System.out.println();
                System.out.println("GENERIRANJE ZAVRSENO USPJESNO!");
                System.out.println("Ukupno vrijeme: " + String.format("%.2f", seconds) + " sekundi");
                System.out.println("Brzina: " + String.format("%.0f", numberOfVideos / seconds) + " videa/sekund");
                generator.showStatistics();
            } else {
                System.err.println("Generiranje neuspjesno!");
            }
            
        } catch (Exception e) {
            System.err.println("KRITICNA GRESKA: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
        System.out.println("Hvala sto koristis Video Generator!");
    }
    
    private boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Uspjesno povezano sa: " + DB_URL);
            
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "videos", null);
            
            if (rs.next()) {
                System.out.println("Tabela 'videos' pronadjena");
                return true;
            } else {
                System.err.println("Tabela 'videos' ne postoji!");
                System.err.println("Pokreni create_tables.sql skriptu prvo");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Konekcija neuspjesna: " + e.getMessage());
            return false;
        }
    }
    
    private void clearVideos() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "DELETE FROM videos";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int deleted = stmt.executeUpdate();
                System.out.println("Obrisano " + deleted + " postojecih videa");
            }
            
        } catch (SQLException e) {
            System.err.println("Greska pri brisanju: " + e.getMessage());
        }
    }
    
    private boolean generateVideos(int totalVideos) {
        Faker faker = new Faker();
        Random random = new Random();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO videos (title, youtube_id, embed_url, positive_votes, negative_votes) VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                System.out.println("Pokretanje generiranja...");
                
                for (int i = 1; i <= totalVideos; i++) {
                    int videoIndex = (i - 1) % 10;
                    String baseYoutubeId = YOUTUBE_VIDEOS[videoIndex][0];
                    
                    // VAŽNO: Koristim originalni YouTube ID za thumbnail (bez "_")
                    // Ali dodajem "_" suffix samo u bazu za unique constraint
                    String uniqueYoutubeId = baseYoutubeId + "_" + i;
                    String embedUrl = "https://www.youtube.com/embed/" + baseYoutubeId;
                    String title = generateTitle(faker, i);
                    int positiveVotes = random.nextInt(1000);
                    int negativeVotes = random.nextInt(500);
                    
                    stmt.setString(1, title);
                    stmt.setString(2, uniqueYoutubeId);  // Unique za bazu
                    stmt.setString(3, embedUrl);
                    stmt.setInt(4, positiveVotes);
                    stmt.setInt(5, negativeVotes);
                    stmt.addBatch();
                    
                    if (i % BATCH_SIZE == 0) {
                        stmt.executeBatch();
                        conn.commit();
                        
                        double progress = (double) i / totalVideos * 100;
                        System.out.printf("Progress: %,d/%,d (%.1f%%)%n", i, totalVideos, progress);
                    }
                }
                
                stmt.executeBatch();
                conn.commit();
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Greska pri insertu: " + e.getMessage());
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Greska konekcije: " + e.getMessage());
            return false;
        }
    }
    
    private String generateTitle(Faker faker, int index) {
        String[] musicCategories = {
            faker.artist().name() + " - " + generateSongTitle(faker),
            faker.artist().name() + " - " + generateSongTitle(faker) + " (Official Video)",
            faker.artist().name() + " - " + generateSongTitle(faker) + " (Official Music Video)",
            faker.artist().name() + " ft. " + faker.artist().name() + " - " + generateSongTitle(faker),
            faker.music().genre() + " Mix " + index,
            "Best " + faker.music().genre() + " Songs " + index,
            "Top " + faker.number().numberBetween(10, 100) + " " + faker.music().genre() + " Hits " + index,
            faker.music().genre() + " Playlist " + index + " - Best Songs",
            "Ultimate " + faker.music().genre() + " Collection " + index,
            faker.artist().name() + " - " + generateSongTitle(faker) + " (Live Performance)",
            faker.artist().name() + " Live Concert " + index,
            faker.artist().name() + " - " + generateSongTitle(faker) + " (Acoustic Version)",
            generateSongTitle(faker) + " - Cover by " + faker.artist().name(),
            faker.artist().name() + " - " + generateSongTitle(faker) + " (Remix)",
            generateSongTitle(faker) + " - " + faker.music().genre() + " Remix " + index,
            faker.artist().name() + " - " + generateSongTitle(faker) + " (Karaoke Version)",
            generateSongTitle(faker) + " - Instrumental " + index,
            faker.artist().name() + " - " + generateSongTitle(faker) + " (Piano Version)",
            faker.artist().name() + " - Greatest Hits (Full Album)",
            "New Song " + index + " - " + faker.artist().name(),
            faker.music().genre() + " Music for Relaxation",
            "1 Hour of " + faker.music().genre() + " Music " + index,
            faker.music().genre() + " Beats " + index + " - Chill Music"
        };
        
        String title = musicCategories[index % musicCategories.length];
        
        if (index % 4 == 0) {
            String[] musicPrefixes = {"♫", "♪", "[MIC]", "[GUITAR]", "[PIANO]", "[DRUMS]", "[MUSIC]", "[LIVE]", "[HOT]", "[NEW]", "[HD]", "[MV]", "[AUDIO]", "[REMIX]", "[COVER]"};
            title = musicPrefixes[index % musicPrefixes.length] + " " + title;
        }
        
        return title;
    }
    
    private String generateSongTitle(Faker faker) {
        String[] formats = {
            faker.color().name() + " " + faker.space().star(),
            faker.ancient().god() + "'s " + faker.shakespeare().hamletQuote().split(" ")[0],
            faker.superhero().power(),
            faker.space().planet() + " Dreams",
            faker.weather().description() + " Night",
            "Dancing " + faker.animal().name(),
            faker.lorem().word() + " Love"
        };
        
        return formats[new Random().nextInt(formats.length)];
    }
    
    private void showStatistics() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            System.out.println();
            System.out.println("STATISTIKE GENERIRANE BAZE");
            System.out.println("==============================");
            
            String countSql = "SELECT COUNT(*) as total FROM videos";
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Ukupno videa: " + String.format("%,d", rs.getInt("total")));
                }
            }
            
            String statsSql = "SELECT " +
                             "SUM(positive_votes) as total_positive, " +
                             "SUM(negative_votes) as total_negative, " +
                             "AVG(positive_votes) as avg_positive, " +
                             "MAX(positive_votes) as max_positive " +
                             "FROM videos";
            try (PreparedStatement stmt = conn.prepareStatement(statsSql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Ukupno pozitivnih glasova: " + String.format("%,d", rs.getInt("total_positive")));
                    System.out.println("Ukupno negativnih glasova: " + String.format("%,d", rs.getInt("total_negative")));
                    System.out.println("Prosjek pozitivnih glasova: " + String.format("%.1f", rs.getDouble("avg_positive")));
                    System.out.println("Najvise pozitivnih glasova: " + rs.getInt("max_positive"));
                }
            }
            
            String distributionSql = "SELECT youtube_id, COUNT(*) as count FROM videos GROUP BY youtube_id ORDER BY count DESC";
            try (PreparedStatement stmt = conn.prepareStatement(distributionSql)) {
                ResultSet rs = stmt.executeQuery();
                System.out.println();
                System.out.println("Distribucija YouTube ID-jeva:");
                while (rs.next()) {
                    System.out.println("   " + rs.getString("youtube_id") + ": " + String.format("%,d", rs.getInt("count")) + " videa");
                }
            }
            
            String sampleSql = "SELECT title, youtube_id FROM videos ORDER BY RAND() LIMIT 5";
            try (PreparedStatement stmt = conn.prepareStatement(sampleSql)) {
                ResultSet rs = stmt.executeQuery();
                System.out.println();
                System.out.println("Sample generirani videi:");
                while (rs.next()) {
                    System.out.println("   " + rs.getString("title") + " (" + rs.getString("youtube_id") + ")");
                }
            }
            
            String sizeSql = "SELECT ROUND(((data_length + index_length) / 1024 / 1024), 2) AS size_mb " +
                            "FROM information_schema.TABLES " +
                            "WHERE table_schema = 'youtube_voting' AND table_name = 'videos'";
            try (PreparedStatement stmt = conn.prepareStatement(sizeSql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println();
                    System.out.println("Velicina tabele: " + rs.getDouble("size_mb") + " MB");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Greska pri prikazivanju statistika: " + e.getMessage());
        }
    }
}