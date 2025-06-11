<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="bs">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rankings - YouTube Voting</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <header>
        <h1>YouTube Voting</h1>
        <nav>
            <ul>
                <li><a href="home">Vote</a></li>
                <li><a href="rankings" class="active">Rankings</a></li>
            </ul>
        </nav>
    </header>
    
    <main>
        <div class="rankings-container">
            <div class="rankings-header">
                <h2>🏆 Video Rankings</h2>
                
            </div>
            
            <c:if test="${not empty error}">
                <div class="error-message">
                    <strong>Greska:</strong> <c:out value="${error}" />
                </div>
            </c:if>
            
            <c:if test="${not empty allVideos}">
                <div class="stats-section">
                    <div class="stat-card videos">
                        <div class="stat-number">${allVideos.size()}</div>
                        <div class="stat-label">Ukupno videa</div>
                    </div>
                    <div class="stat-card votes">
                        <div class="stat-number">
                            <c:set var="totalVotes" value="0" />
                            <c:forEach var="video" items="${allVideos}">
                                <c:set var="totalVotes" value="${totalVotes + video.positiveVotes + video.negativeVotes}" />
                            </c:forEach>
                            ${totalVotes}
                        </div>
                        <div class="stat-label">Ukupno glasova</div>
                    </div>
                    <div class="stat-card top">
                        <div class="stat-number">
                            <c:choose>
                                <c:when test="${not empty maxPositiveVotes}">
                                    ${maxPositiveVotes}
                                </c:when>
                                <c:when test="${not empty allVideos}">
                                    ${allVideos[0].positiveVotes}
                                </c:when>
                                <c:otherwise>
                                    0
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="stat-label">Najveci broj glasova</div>
                    </div>
                    <div class="stat-card positive">
                        <div class="stat-number">
                            <c:set var="totalPositiveVotes" value="0" />
                            <c:forEach var="video" items="${allVideos}">
                                <c:set var="totalPositiveVotes" value="${totalPositiveVotes + video.positiveVotes}" />
                            </c:forEach>
                            ${totalPositiveVotes}
                        </div>
                        <div class="stat-label">Pozitivni glasovi</div>
                    </div>
                </div>
                
                <div class="action-buttons">
                    <a href="home" class="btn btn-success">
                        🗳️ Glasaj za videe
                    </a>
                    <a href="rankings" class="btn btn-primary">
                        🔄 Osvježi rankings
                    </a>
                </div>
                
                <table class="rankings-table">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Naslov</th>
                            <th>Thumbnail</th>
                            <th>👍 Pozitivni</th>
                            <th>📊 Ukupno</th>
                            <th>📈 Wilson Score</th>
                            <th>⭐ Kvalitet</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="video" items="${allVideos}" varStatus="status">
                            <c:set var="globalRank" value="${(currentPage - 1) * pageSize + status.index + 1}" />
                            <tr>
                                <td class="rank-cell <c:choose><c:when test='${globalRank == 1}'>first</c:when><c:when test='${globalRank == 2}'>second</c:when><c:when test='${globalRank == 3}'>third</c:when></c:choose>">
                                    ${globalRank}
                                    <c:if test="${globalRank == 1}">🥇</c:if>
                                    <c:if test="${globalRank == 2}">🥈</c:if>
                                    <c:if test="${globalRank == 3}">🥉</c:if>
                                </td>
                                <td class="title-header-cell">
                                    <c:out value="${video.title}" />
                                    <c:if test="${video.negativeVotes > 0}">
                                        <br><small>
                                            Score: ${video.positiveVotes - video.negativeVotes} 
                                            (👍${video.positiveVotes} / 👎${video.negativeVotes})
                                        </small>
                                    </c:if>
                                </td>
                                <td class="thumbnail-cell">
                                    <img src="https://img.youtube.com/vi/<c:out value='${video.youtubeId}' />/mqdefault.jpg" 
                                         alt="<c:out value='${video.title}' />"
                                         onerror="this.src='https://via.placeholder.com/100x75/cccccc/666666?text=No+Image'">
                                </td>
                                <td class="votes-cell positive-votes">
                                    ${video.positiveVotes}
                                </td>
                                <td class="votes-cell total-votes">
                                    ${video.positiveVotes + video.negativeVotes}
                                    <c:if test="${(video.positiveVotes + video.negativeVotes) > 0}">
                                        <br><small style="color: #aaa;">
                                            ${video.positiveVotes + video.negativeVotes > 0 ? 
                                              Math.round((video.positiveVotes * 100.0) / (video.positiveVotes + video.negativeVotes)) : 0}% pozitivnih
                                        </small>
                                    </c:if>
                                </td>
                                <td class="wilson-score-cell">
                                    <c:set var="totalVotes" value="${video.positiveVotes + video.negativeVotes}" />
                                    <c:choose>
                                        <c:when test="${totalVotes > 0 and video.wilsonScore > 0}">
                                            <span class="wilson-score-value">
                                                <c:out value="${Math.round(video.wilsonScore * 10000) / 100.0}"/>%
                                            </span>
                                            <br><small style="color: #aaa;">
                                                Score: <c:out value="${Math.round(video.wilsonScore * 10000) / 10000.0}"/>
                                            </small>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #666;">
                                                N/A (Wilson: ${video.wilsonScore}, Total: ${totalVotes})
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="quality-rating-cell">
                                    <c:set var="rating" value="${video.wilsonScore * 5}" />
                                    <c:choose>
                                        <c:when test="${video.wilsonScore > 0}">
                                            <div class="star-rating">
                                                <c:forEach begin="1" end="5" var="star">
                                                    <c:choose>
                                                        <c:when test="${rating >= star}">⭐</c:when>
                                                        <c:when test="${rating >= (star - 0.5)}">🌟</c:when>
                                                        <c:otherwise>☆</c:otherwise>
                                                    </c:choose>
                                                </c:forEach>
                                            </div>
                                            <small style="color: #aaa;">
                                                <c:out value="${Math.round(rating * 10) / 10.0}"/>/5.0
                                            </small>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #666;">Nema ocjenu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
                
                <c:if test="${totalPages > 1}">
                    <div class="pagination-container">
                        <div class="pagination">
                            <c:if test="${hasPrevious}">
                                <a href="rankings?page=${currentPage - 1}" class="page-link prev">
                                    &lt; Prethodna
                                </a>
                            </c:if>
                            <c:if test="${!hasPrevious}">
                                <span class="page-link prev disabled">&lt; Prethodna</span>
                            </c:if>
                            
                            <c:choose>
                                <c:when test="${totalPages <= 7}">
                                    <c:forEach begin="1" end="${totalPages}" var="pageNum">
                                        <c:choose>
                                            <c:when test="${pageNum == currentPage}">
                                                <span class="page-link current">${pageNum}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="rankings?page=${pageNum}" class="page-link">${pageNum}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${currentPage > 3}">
                                        <a href="rankings?page=1" class="page-link">1</a>
                                        <c:if test="${currentPage > 4}">
                                            <span class="page-dots">...</span>
                                        </c:if>
                                    </c:if>
                                    
                                    <c:forEach begin="${currentPage - 2 < 1 ? 1 : currentPage - 2}" 
                                              end="${currentPage + 2 > totalPages ? totalPages : currentPage + 2}" 
                                              var="pageNum">
                                        <c:choose>
                                            <c:when test="${pageNum == currentPage}">
                                                <span class="page-link current">${pageNum}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="rankings?page=${pageNum}" class="page-link">${pageNum}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                    
                                    <c:if test="${currentPage < totalPages - 2}">
                                        <c:if test="${currentPage < totalPages - 3}">
                                            <span class="page-dots">...</span>
                                        </c:if>
                                        <a href="rankings?page=${totalPages}" class="page-link">${totalPages}</a>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                            
                            <c:if test="${hasNext}">
                                <a href="rankings?page=${currentPage + 1}" class="page-link next">
                                    Sljedeća &gt;
                                </a>
                            </c:if>
                            <c:if test="${!hasNext}">
                                <span class="page-link next disabled">Sljedeća &gt;</span>
                            </c:if>
                        </div>
                        
                        <div class="page-info-bottom">
                            Stranica ${currentPage} od ${totalPages} 
                            (${totalVideos} ukupno videa)
                        </div>
                    </div>
                </c:if>
            </c:if>
            
            <c:if test="${empty allVideos}">
                <div class="empty-state">
                    <h3>📹 Nema videa u bazi</h3>
                    <p>Dodajte videe da biste mogli vidjeti rankings.</p>
                    <a href="home" class="btn btn-primary">🏠 Idite na početnu stranicu</a>
                </div>
            </c:if>
            
            <div class="rankings-footer">
                <p><em>Rankings se azuriraju u realnom vremenu na osnovu glasova korisnika</em></p>
                <p>Posljednje azurirano: <span id="lastUpdated"></span></p>
            </div>
        </div>
    </main>
    
    <script>
        document.getElementById('lastUpdated').textContent = new Date().toLocaleString('bs-BA');
    </script>
</body>
</html>