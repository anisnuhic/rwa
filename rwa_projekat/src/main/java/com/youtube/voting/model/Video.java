package com.youtube.voting.model;


public class Video {
    private int id;
    private String title;
    private String youtubeId;
    private String embedUrl;
    private int positiveVotes;
    private int negativeVotes;
    private String createdAt;
    private double wilsonScore = 0.0; // NOVO POLJE sa default vrijednošću
    
    // Konstruktori
    public Video() {}
    
    public Video(int id, String title, String youtubeId, String embedUrl, int positiveVotes, int negativeVotes) {
        this.id = id;
        this.title = title;
        this.youtubeId = youtubeId;
        this.embedUrl = embedUrl;
        this.positiveVotes = positiveVotes;
        this.negativeVotes = negativeVotes;
    }
    
    // Osnovni getteri i setteri
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getYoutubeId() {
        return youtubeId;
    }
    
    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }
    
    public String getEmbedUrl() {
        return embedUrl;
    }
    
    public void setEmbedUrl(String embedUrl) {
        this.embedUrl = embedUrl;
    }
    
    public int getPositiveVotes() {
        return positiveVotes;
    }
    
    public void setPositiveVotes(int positiveVotes) {
        this.positiveVotes = positiveVotes;
    }
    
    public int getNegativeVotes() {
        return negativeVotes;
    }
    
    public void setNegativeVotes(int negativeVotes) {
        this.negativeVotes = negativeVotes;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    // WILSON SCORE getteri i setteri
    public double getWilsonScore() {
        return wilsonScore;
    }
    
    public void setWilsonScore(double wilsonScore) {
        this.wilsonScore = wilsonScore;
    }
    
    // NOVE metode za Wilson Score formatiranje
    public String getFormattedWilsonScore() {
        return String.format("%.4f", wilsonScore);
    }
    
    public String getWilsonScorePercentage() {
        return String.format("%.2f%%", wilsonScore * 100);
    }
    
    public double getConfidenceRating() {
        return wilsonScore * 5.0; // Za prikaz kao rating od 0-5
    }
    
    // Metode za računanje ukupnih glasova i score-a
    public int getTotalVotes() {
        return positiveVotes + negativeVotes;
    }
    
    public int getScore() {
        return positiveVotes - negativeVotes;
    }
    
    // Metoda za procenat pozitivnih glasova
    public double getPositivePercentage() {
        if (getTotalVotes() == 0) return 0.0;
        return (double) positiveVotes / getTotalVotes() * 100.0;
    }
    
    // Za backward compatibility (stara metoda)
    public int getVotes() {
        return positiveVotes;
    }
    
    public void setVotes(int votes) {
        this.positiveVotes = votes;
    }
    
    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", youtubeId='" + youtubeId + '\'' +
                ", positiveVotes=" + positiveVotes +
                ", negativeVotes=" + negativeVotes +
                ", wilsonScore=" + String.format("%.4f", wilsonScore) +
                '}';
    }
}