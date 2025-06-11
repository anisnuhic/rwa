package com.youtube.voting.servlet;

import com.youtube.voting.dao.VideoDAO;
import com.youtube.voting.model.Video;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/vote")
public class VoteServlet extends HttpServlet {
    
    private VideoDAO videoDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        videoDAO = new VideoDAO();
        gson = new Gson();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String ajaxHeader = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);
        
        if (isAjax) {
            handleAjaxVote(request, response);
        } else {
            handleRegularVote(request, response);
        }
    }
    
    private void handleAjaxVote(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        try {
            String positiveVideoIdParam = request.getParameter("positiveVideoId");
            String negativeVideoIdParam = request.getParameter("negativeVideoId");
            
            if (positiveVideoIdParam == null || negativeVideoIdParam == null || 
                positiveVideoIdParam.trim().isEmpty() || negativeVideoIdParam.trim().isEmpty()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Video ID-jevi nisu specificirani!");
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            int positiveVideoId, negativeVideoId;
            
            try {
                positiveVideoId = Integer.parseInt(positiveVideoIdParam.trim());
                negativeVideoId = Integer.parseInt(negativeVideoIdParam.trim());
            } catch (NumberFormatException e) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Nevazeći video ID format!");
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            if (positiveVideoId == negativeVideoId) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Ne mozete glasati za isti video pozitivno i negativno!");
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            List<Video> newVideos = videoDAO.voteInPairAndGetNewVideos(positiveVideoId, negativeVideoId);
            
            if (newVideos != null && newVideos.size() == 2) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Hvala na glasanju!");
                jsonResponse.add("newVideos", gson.toJsonTree(newVideos));
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("error", "Greska pri glasovanju ili dohvatanju novih videa. Dobijeno videa: " + 
                                       (newVideos != null ? newVideos.size() : "null"));
            }
            
        } catch (Exception e) {
            System.err.println("Greska u Ajax glasovanju: " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("error", "Sistemska greska: " + e.getMessage());
        }
        
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
    
    private void handleRegularVote(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String positiveVideoIdParam = request.getParameter("positiveVideoId");
        String negativeVideoIdParam = request.getParameter("negativeVideoId");
        
        if (positiveVideoIdParam == null || negativeVideoIdParam == null ||
            positiveVideoIdParam.trim().isEmpty() || negativeVideoIdParam.trim().isEmpty()) {
            request.setAttribute("error", "Morate poslati oba video ID-ja za glasanje u paru!");
            request.getRequestDispatcher("/home").forward(request, response);
            return;
        }
        
        try {
            int positiveVideoId = Integer.parseInt(positiveVideoIdParam.trim());
            int negativeVideoId = Integer.parseInt(negativeVideoIdParam.trim());
            
            if (positiveVideoId == negativeVideoId) {
                request.setAttribute("error", "Ne mozete glasati za isti video pozitivno i negativno!");
                request.getRequestDispatcher("/home").forward(request, response);
                return;
            }
            
            List<Video> newVideos = videoDAO.voteInPairAndGetNewVideos(positiveVideoId, negativeVideoId);
            
            if (newVideos != null && newVideos.size() == 2) {
                request.setAttribute("success", "Hvala na glasanju!");
                request.setAttribute("newVideos", newVideos);
            } else {
                request.setAttribute("error", "Greska pri glasovanju ili dohvatanju novih videa.");
            }
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Nevazeći video ID!");
        } catch (Exception e) {
            request.setAttribute("error", "Sistemska greska pri glasovanju.");
            e.printStackTrace();
        }
        
        request.getRequestDispatcher("/home").forward(request, response);
    }
}