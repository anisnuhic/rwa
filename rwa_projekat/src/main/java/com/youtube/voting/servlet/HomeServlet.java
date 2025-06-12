package com.youtube.voting.servlet;

import com.youtube.voting.dao.VideoDAO;
import com.youtube.voting.model.Video;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    
    private VideoDAO videoDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        videoDAO = new VideoDAO();
        System.out.println("HomeServlet inicijalizovan!");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            List<Video> videosToShow;
            
            // Provjeri da li su specificirani video ID-jevi u URL-u
            String video1Param = request.getParameter("video1");
            String video2Param = request.getParameter("video2");
            
            if (video1Param != null && video2Param != null && 
                !video1Param.trim().isEmpty() && !video2Param.trim().isEmpty()) {
                
                try {
                    int video1Id = Integer.parseInt(video1Param.trim());
                    int video2Id = Integer.parseInt(video2Param.trim());
                    
                    System.out.println("Pokušavam dohvatiti specifične videe: " + video1Id + ", " + video2Id);
                    
                    // Dohvati specifične videe
                    videosToShow = videoDAO.getSpecificVideos(video1Id, video2Id);
                    
                    // Provjeri da li su oba videa pronađena
                    if (videosToShow.size() != 2) {
                        System.out.println("Specifični videi nisu pronađeni (dobijeno: " + videosToShow.size() + " videa), koristim random videe");
                        videosToShow = videoDAO.getRandomVideos(2);
                    } else {
                        System.out.println("Uspješno učitani specifični videi: " + video1Id + ", " + video2Id);
                    }
                    
                } catch (NumberFormatException e) {
                    System.out.println("Neispravni video ID parametri (" + video1Param + ", " + video2Param + "), koristim random videe");
                    videosToShow = videoDAO.getRandomVideos(2);
                }
                
            } else {
                // Normalno učitavanje random videa
                System.out.println("Učitavam 2 random videa");
                videosToShow = videoDAO.getRandomVideos(2);
            }
            
            // Dohvati top 5 videa za mini ranking
            System.out.println("Dohvaćam Top 5 videa...");
            List<Video> top5Videos = videoDAO.getTop5Videos();
            
            // Debug informacije
            System.out.println("=== DEBUG INFORMACIJE ===");
            System.out.println("Random/Specific videi: " + videosToShow.size());
            for (Video video : videosToShow) {
                System.out.println("- " + video.getTitle() + " (ID: " + video.getId() + ", Votes: " + video.getPositiveVotes() + ")");
            }
            
            System.out.println("Top 5 videi: " + top5Videos.size());
            for (Video video : top5Videos) {
                System.out.println("- " + video.getTitle() + " (ID: " + video.getId() + ", Votes: " + video.getPositiveVotes() + ")");
            }
            System.out.println("========================");
            
            // Provjeri da li ima videa za prikaz
            if (videosToShow.isEmpty()) {
                System.out.println("GREŠKA: Nema videa za prikaz!");
                request.setAttribute("error", "Nema dostupnih videa u bazi.");
            } else {
                System.out.println("Uspješno učitano " + videosToShow.size() + " videa za glasovanje");
            }
            
            // Postavi atribute za JSP
            request.setAttribute("randomVideos", videosToShow);
            request.setAttribute("top5Videos", top5Videos);
            
            // Debug atributa
            System.out.println("Postavljeni atributi:");
            System.out.println("- randomVideos: " + (request.getAttribute("randomVideos") != null ? "SET" : "NULL"));
            System.out.println("- top5Videos: " + (request.getAttribute("top5Videos") != null ? "SET" : "NULL"));
            
            // Proslijedi zahtjev na JSP stranicu
            System.out.println("Preusmjeravam na index.jsp");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("GREŠKA u HomeServlet: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", "Greška pri učitavanju videa: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}