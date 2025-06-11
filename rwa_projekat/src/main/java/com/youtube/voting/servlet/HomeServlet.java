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
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            List<Video> randomVideos = videoDAO.getRandomVideos(2);
            List<Video> top5Videos = videoDAO.getTop5Videos();
            
            if (randomVideos.isEmpty()) {
                request.setAttribute("error", "Nema dostupnih videa u bazi.");
            }
            
            request.setAttribute("randomVideos", randomVideos);
            request.setAttribute("top5Videos", top5Videos);
            
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Greska u HomeServlet: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", "Greska pri ucitavanju videa: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}