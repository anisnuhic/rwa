package com.youtube.voting.dao;

import com.youtube.voting.database.DatabaseConnection;
import com.youtube.voting.model.Video;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VideoDAO {

    public List<Video> voteInPairAndGetNewVideos(int positiveVideoId, int negativeVideoId) {
        List<Video> newVideos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String positiveSql = "UPDATE videos SET positive_votes = positive_votes + 1 WHERE id = ?";
                try (PreparedStatement stmt1 = conn.prepareStatement(positiveSql)) {
                    stmt1.setInt(1, positiveVideoId);
                    int rows1 = stmt1.executeUpdate();
                    if (rows1 == 0) {
                        throw new SQLException("Video sa ID " + positiveVideoId + " nije pronadjen.");
                    }
                }

                String negativeSql = "UPDATE videos SET negative_votes = negative_votes + 1 WHERE id = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(negativeSql)) {
                    stmt2.setInt(1, negativeVideoId);
                    int rows2 = stmt2.executeUpdate();
                    if (rows2 == 0) {
                        throw new SQLException("Video sa ID " + negativeVideoId + " nije pronadjen.");
                    }
                }

                conn.commit();
                newVideos = getRandomVideos(2);
                return newVideos;

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Greska pri rollback-u: " + rollbackEx.getMessage());
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Greska pri vracanju autoCommit postavke: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Greska pri glasovanju i dohvacanju novih videa: " + e.getMessage());
            e.printStackTrace();
        }

        return newVideos;
    }

    public List<Video> getRandomVideos(int count) {
        List<Video> videos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String countSql = "SELECT COUNT(*) FROM videos";
            int totalVideos = 0;

            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                ResultSet countRs = countStmt.executeQuery();
                if (countRs.next()) {
                    totalVideos = countRs.getInt(1);
                }
            }

            if (totalVideos == 0) {
                return videos;
            }

            if (totalVideos < count) {
                count = totalVideos;
            }

            Random random = new Random();
            int maxOffset = Math.max(0, totalVideos - count);
            int offset = maxOffset > 0 ? random.nextInt(maxOffset + 1) : 0;

            String selectSql = "SELECT * FROM videos LIMIT ? OFFSET ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, count);
                selectStmt.setInt(2, offset);
                ResultSet rs = selectStmt.executeQuery();

                while (rs.next()) {
                    Video video = new Video();
                    video.setId(rs.getInt("id"));
                    video.setTitle(rs.getString("title"));
                    
                    String youtubeId = rs.getString("youtube_id");
                    // Za thumbnail, koristi originalni ID (bez "_" suffix-a)
                    String thumbnailId = youtubeId.contains("_") ? youtubeId.split("_")[0] : youtubeId;
                    video.setYoutubeId(thumbnailId);  // Postavi originalni ID za thumbnail
                    
                    video.setEmbedUrl(rs.getString("embed_url"));
                    video.setPositiveVotes(rs.getInt("positive_votes"));
                    video.setNegativeVotes(rs.getInt("negative_votes"));
                    video.setCreatedAt(rs.getString("created_at"));

                    videos.add(video);
                }
            }

        } catch (SQLException e) {
            System.err.println("Greska pri dohvacanju random videa: " + e.getMessage());
            e.printStackTrace();
        }

        return videos;
    }

    public List<Video> getTop5Videos() {
        List<Video> topVideos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM videos ORDER BY positive_votes DESC LIMIT 5";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Video video = new Video();
                    video.setId(rs.getInt("id"));
                    video.setTitle(rs.getString("title"));
                    video.setYoutubeId(rs.getString("youtube_id"));
                    video.setEmbedUrl(rs.getString("embed_url"));
                    video.setPositiveVotes(rs.getInt("positive_votes"));
                    video.setNegativeVotes(rs.getInt("negative_votes"));
                    video.setCreatedAt(rs.getString("created_at"));
                    
                    topVideos.add(video);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Greska pri dohvacanju top 5 videa: " + e.getMessage());
            e.printStackTrace();
        }
        
        return topVideos;
    }

    public List<Video> getAllVideosSortedByVotes() {
        List<Video> videos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM videos ORDER BY positive_votes DESC, title ASC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Video video = new Video();
                    video.setId(rs.getInt("id"));
                    video.setTitle(rs.getString("title"));
                    video.setYoutubeId(rs.getString("youtube_id"));
                    video.setEmbedUrl(rs.getString("embed_url"));
                    video.setPositiveVotes(rs.getInt("positive_votes"));
                    video.setNegativeVotes(rs.getInt("negative_votes"));
                    video.setCreatedAt(rs.getString("created_at"));
                    
                    videos.add(video);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Greska pri dohvacanju svih videa za rankings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return videos;
    }

    public List<Video> getAllVideosSortedByWilsonScore() {
        List<Video> videos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT *, " +
                         "CASE " +
                         "    WHEN (positive_votes + negative_votes) = 0 THEN 0 " +
                         "    ELSE ( " +
                         "        (positive_votes + 1.9208) / (positive_votes + negative_votes) - " +
                         "        1.96 * SQRT((positive_votes * negative_votes) / (positive_votes + negative_votes) + 0.9604) / " +
                         "        (positive_votes + negative_votes) " +
                         "    ) / (1 + 3.8416 / (positive_votes + negative_votes)) " +
                         "END AS wilson_score " +
                         "FROM videos " +
                         "ORDER BY wilson_score DESC, positive_votes DESC, id ASC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Video video = new Video();
                    video.setId(rs.getInt("id"));
                    video.setTitle(rs.getString("title"));
                    video.setYoutubeId(rs.getString("youtube_id"));
                    video.setEmbedUrl(rs.getString("embed_url"));
                    video.setPositiveVotes(rs.getInt("positive_votes"));
                    video.setNegativeVotes(rs.getInt("negative_votes"));
                    video.setCreatedAt(rs.getString("created_at"));
                    
                    double wilsonScore = rs.getDouble("wilson_score");
                    video.setWilsonScore(wilsonScore);
                    
                    videos.add(video);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Greska pri dohvacanju videa sa Wilson Score: " + e.getMessage());
            e.printStackTrace();
        }
        
        return videos;
    }

    public double calculateWilsonScore(int positive, int total) {
        if (total == 0) return 0;
        
        double z = 1.96;
        double phat = (double) positive / total;
        
        double numerator = phat + (z * z) / (2 * total) - z * Math.sqrt((phat * (1 - phat) + (z * z) / (4 * total)) / total);
        double denominator = 1 + (z * z) / total;
        
        return numerator / denominator;
    }

    public List<Video> getAllVideosSortedByWilsonScoreJava() {
        List<Video> videos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM videos";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Video video = new Video();
                    video.setId(rs.getInt("id"));
                    video.setTitle(rs.getString("title"));
                    video.setYoutubeId(rs.getString("youtube_id"));
                    video.setEmbedUrl(rs.getString("embed_url"));
                    video.setPositiveVotes(rs.getInt("positive_votes"));
                    video.setNegativeVotes(rs.getInt("negative_votes"));
                    video.setCreatedAt(rs.getString("created_at"));
                    
                    int totalVotes = video.getPositiveVotes() + video.getNegativeVotes();
                    double wilsonScore = calculateWilsonScore(video.getPositiveVotes(), totalVotes);
                    video.setWilsonScore(wilsonScore);
                    
                    videos.add(video);
                }
            }
            
            videos.sort((v1, v2) -> {
                int scoreCompare = Double.compare(v2.getWilsonScore(), v1.getWilsonScore());
                if (scoreCompare != 0) return scoreCompare;
                
                int votesCompare = Integer.compare(v2.getPositiveVotes(), v1.getPositiveVotes());
                if (votesCompare != 0) return votesCompare;
                
                return Integer.compare(v1.getId(), v2.getId());
            });
            
        } catch (SQLException e) {
            System.err.println("Greska pri dohvacanju videa: " + e.getMessage());
            e.printStackTrace();
        }
        
        return videos;
    }

    public List<Video> getAllVideosSortedByWilsonScoreWithPagination(int page, int pageSize) {
        List<Video> videos = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT *, " +
                         "CASE " +
                         "    WHEN (positive_votes + negative_votes) = 0 THEN 0 " +
                         "    ELSE ( " +
                         "        (positive_votes + 1.9208) / (positive_votes + negative_votes) - " +
                         "        1.96 * SQRT((positive_votes * negative_votes) / (positive_votes + negative_votes) + 0.9604) / " +
                         "        (positive_votes + negative_votes) " +
                         "    ) / (1 + 3.8416 / (positive_votes + negative_votes)) " +
                         "END AS wilson_score " +
                         "FROM videos " +
                         "ORDER BY wilson_score DESC, positive_votes DESC, id ASC " +
                         "LIMIT ? OFFSET ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, pageSize);
                stmt.setInt(2, offset);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Video video = new Video();
                    video.setId(rs.getInt("id"));
                    video.setTitle(rs.getString("title"));
                    video.setYoutubeId(rs.getString("youtube_id"));
                    video.setEmbedUrl(rs.getString("embed_url"));
                    video.setPositiveVotes(rs.getInt("positive_votes"));
                    video.setNegativeVotes(rs.getInt("negative_votes"));
                    video.setCreatedAt(rs.getString("created_at"));
                    
                    double wilsonScore = rs.getDouble("wilson_score");
                    video.setWilsonScore(wilsonScore);
                    
                    videos.add(video);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Greska pri dohvacanju paginiranih videa: " + e.getMessage());
            e.printStackTrace();
        }
        
        return videos;
    }

    public int getTotalVideoCount() {
        int count = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) as total FROM videos";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    count = rs.getInt("total");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Greska pri brojanju videa: " + e.getMessage());
            e.printStackTrace();
        }
        
        return count;
    }

    public int getTotalPages(int pageSize) {
        int totalVideos = getTotalVideoCount();
        return (int) Math.ceil((double) totalVideos / pageSize);
    }

    public boolean isValidPage(int page, int pageSize) {
        int totalPages = getTotalPages(pageSize);
        return page >= 1 && page <= totalPages;
    }
}