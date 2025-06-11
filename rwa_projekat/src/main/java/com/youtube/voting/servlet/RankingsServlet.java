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

@WebServlet("/rankings")
public class RankingsServlet extends HttpServlet {
    
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
            String pageParam = request.getParameter("page");
            int currentPage = 1;
            int pageSize = 20;
            
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    currentPage = Integer.parseInt(pageParam);
                    if (currentPage < 1) currentPage = 1;
                } catch (NumberFormatException e) {
                    currentPage = 1;
                }
            }
            
            int totalVideos = videoDAO.getTotalVideoCount();
            int totalPages = videoDAO.getTotalPages(pageSize);
            
            if (!videoDAO.isValidPage(currentPage, pageSize) && totalPages > 0) {
                currentPage = 1;
            }
            
            List<Video> allVideos = videoDAO.getAllVideosSortedByWilsonScoreWithPagination(currentPage, pageSize);
            
            if (allVideos.isEmpty() && totalVideos > 0) {
                List<Video> allVideosNoPaging = videoDAO.getAllVideosSortedByWilsonScore();
                
                int startIndex = (currentPage - 1) * pageSize;
                int endIndex = Math.min(startIndex + pageSize, allVideosNoPaging.size());
                
                if (startIndex < allVideosNoPaging.size()) {
                    allVideos = allVideosNoPaging.subList(startIndex, endIndex);
                }
            }
            
            int maxPositiveVotesOnPage = 0;
            for (Video video : allVideos) {
                if (video.getPositiveVotes() > maxPositiveVotesOnPage) {
                    maxPositiveVotesOnPage = video.getPositiveVotes();
                }
            }
            
            boolean hasPrevious = currentPage > 1;
            boolean hasNext = currentPage < totalPages;
            int startVideo = totalVideos > 0 ? (currentPage - 1) * pageSize + 1 : 0;
            int endVideo = Math.min(currentPage * pageSize, totalVideos);
            
            request.setAttribute("allVideos", allVideos);
            request.setAttribute("maxPositiveVotes", maxPositiveVotesOnPage);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalVideos", totalVideos);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("hasPrevious", hasPrevious);
            request.setAttribute("hasNext", hasNext);
            request.setAttribute("startVideo", startVideo);
            request.setAttribute("endVideo", endVideo);
            
            request.getRequestDispatcher("/rankings.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Greska u RankingsServlet: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("error", "Greska pri ucitavanju rankings: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}