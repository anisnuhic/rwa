<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    if (request.getAttribute("randomVideos") == null) {
        response.sendRedirect("home");
        return;
    }
%>
<!DOCTYPE html>
<html lang="bs">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>YouTube Voting</title>
    <link rel="stylesheet" href="css/style.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
    <header>
        <h1>YouTube Voting</h1>
        <nav>
            <ul>
                <li><a href="home">Vote</a></li>
                <li><a href="rankings">Rankings</a></li>
            </ul>
        </nav>
    </header>
    
    <main class="page-container">
        <div class="voting-section">
            <h2>Vote for your favourite video!</h2>
            
            <div id="messageContainer" class="message-container"></div>
            
            <c:if test="${not empty error}">
                <div class="error-message">
                    <strong>Greska:</strong> <c:out value="${error}" />
                </div>
            </c:if>
            
            <div id="videoContainer" class="video-list">
                <c:if test="${not empty randomVideos}">
                    <c:forEach var="video" items="${randomVideos}" varStatus="status">
                        <div class="video-item" data-video-id="${video.id}">
                            <h3><c:out value="${video.title}" /></h3>
                            <iframe width="560" height="315" 
                                    src="<c:out value='${video.embedUrl}' />" 
                                    title="YouTube video player" 
                                    frameborder="0" 
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" 
                                    referrerpolicy="strict-origin-when-cross-origin" 
                                    allowfullscreen>
                            </iframe>
                            <button type="button" class="vote-btn" onclick="voteForVideo('${video.id}')">
                                🗳️ Vote for this video!
                            </button>
                        </div>
                    </c:forEach>
                </c:if>
                
                <c:if test="${empty randomVideos}">
                    <div class="empty-state">
                        <h3>Nema dostupnih videa za glasovanje.</h3>
                        <p>Molimo pokusajte kasnije.</p>
                    </div>
                </c:if>
            </div>
            
            <div id="loadingIndicator" class="loading-indicator">
                <p>Procesiranje glasovanja...</p>
            </div>
            
            <div class="info-section">
                <p><em>Klikom na "Vote for this video!" dajete pozitivan glas odabranom videu i negativan glas drugom videu!</em></p>
                
                
                <div class="share-section">
                    <button type="button" class="share-btn" onclick="toggleShareOptions()">
                        📤 Podijeli trenutni par videa
                    </button>
                    
                    <div id="shareOptions" class="share-options">
                        <h4>📱 Podijeli na socijalnim mrežama</h4>
                        
                        <div class="social-links">
                            <a href="#" class="social-link twitter" onclick="shareOnTwitter()">
                                🐦 Twitter
                            </a>
                            <a href="#" class="social-link facebook" onclick="shareOnFacebook()">
                                👥 Facebook
                            </a>
                            <a href="#" class="social-link linkedin" onclick="shareOnLinkedIn()">
                                💼 LinkedIn
                            </a>
                            <a href="#" class="social-link whatsapp" onclick="shareOnWhatsApp()">
                                💬 WhatsApp
                            </a>
                        </div>
                        
                        <div class="copy-section">
                            <p style="color: #ccc; margin-bottom: 10px; font-size: 14px;">📋 Ili kopiraj link:</p>
                            <div class="copy-container">
                                <input type="text" id="shareUrl" class="share-url" readonly>
                                <button type="button" class="copy-btn" onclick="copyToClipboard()">
                                    📋 Kopiraj
                                </button>
                            </div>
                        </div>
                        
                        <button type="button" class="share-close" onclick="closeShareOptions()">
                            ✖ Zatvori
                        </button>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="ranking-section">
            <div class="mini-ranking">
                <h3>🏆 Top 5 Videos</h3>
                
                <c:if test="${not empty top5Videos}">
                    <table>
                        <thead>
                            <tr>
                                <th>Rank</th>
                                <th>Thumbnail</th>
                                <th>Naslov</th>
                                <th>Glasovi</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="video" items="${top5Videos}" varStatus="status">
                                <tr>
                                    <td class="rank-cell">${status.index + 1}</td>
                                    <td class="thumbnail-cell">
                                        <img src="https://img.youtube.com/vi/<c:out value='${video.youtubeId}' />/mqdefault.jpg" 
                                             alt="<c:out value='${video.title}' />">
                                    </td>
                                    <td class="title-cell">
                                        <c:out value="${video.title}" />
                                    </td>
                                    <td class="votes-cell">${video.positiveVotes}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
                
                <c:if test="${empty top5Videos}">
                    <p style="text-align: center; color: #6c757d; margin: 20px 0;">
                        Nema dovoljno videa za prikaz ranking-a.
                    </p>
                </c:if>
                
                <div>
                    <a href="rankings">
                        📊 Pogledaj kompletne rezultate →
                    </a>
                </div>
            </div>
        </div>
    </main>

    <script>
        // Provjeri da li je ovo dijeljeni link (ima video1 i video2 parametre)
        function isSharedLink() {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.has('video1') && urlParams.has('video2');
        }
        
        function voteForVideo(positiveVideoId) {
            const videoContainers = document.querySelectorAll('.video-item');
            let negativeVideoId = null;
            
            videoContainers.forEach(container => {
                const videoId = container.getAttribute('data-video-id');
                
                if (parseInt(videoId) !== parseInt(positiveVideoId)) {
                    negativeVideoId = parseInt(videoId);
                }
            });
            
            if (!negativeVideoId) {
                showMessage('Greska: Nije moguce identificirati drugi video!', 'error');
                return;
            }
            
            document.getElementById('loadingIndicator').style.display = 'block';
            
            const voteButtons = document.querySelectorAll('.vote-btn');
            voteButtons.forEach(btn => {
                btn.disabled = true;
                btn.style.opacity = '0.5';
            });
            
            $.ajax({
                url: 'vote',
                type: 'POST',
                data: {
                    positiveVideoId: positiveVideoId,
                    negativeVideoId: negativeVideoId
                },
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                dataType: 'json',
                timeout: 10000,
                success: function(response) {
                    if (response.success) {
                        showMessage(response.message || 'Hvala na glasanju!', 'success');
                        
                        if (response.newVideos && response.newVideos.length === 2) {
                            updateVideos(response.newVideos);
                            // Ukloni URL parametre nakon glasovanja da sljedeći refresh bude random
                            clearUrlParameters();
                        } else {
                            showMessage('Glasovanje uspjesno, ali nema novih videa za prikaz.', 'warning');
                        }
                    } else {
                        showMessage(response.error || 'Nepoznata greska', 'error');
                    }
                },
                error: function(xhr, status, error) {
                    let errorMessage = 'Greska pri komunikaciji sa serverom';
                    if (status === 'timeout') {
                        errorMessage = 'Zahtjev je istekao - pokusajte ponovo';
                    } else if (xhr.status === 404) {
                        errorMessage = 'Server endpoint nije pronadjen';
                    } else if (xhr.status === 500) {
                        errorMessage = 'Greska na serveru';
                    }
                    
                    showMessage(errorMessage, 'error');
                },
                complete: function() {
                    document.getElementById('loadingIndicator').style.display = 'none';
                    
                    const voteButtons = document.querySelectorAll('.vote-btn');
                    voteButtons.forEach(btn => {
                        btn.disabled = false;
                        btn.style.opacity = '1';
                    });
                }
            });
        }
        
        // Nova funkcija za uklanjanje URL parametara
        function clearUrlParameters() {
            if (window.history && window.history.replaceState) {
                const url = window.location.protocol + "//" + window.location.host + window.location.pathname;
                window.history.replaceState({ path: url }, '', url);
            }
        }
        
        function updateVideos(newVideos) {
            const videoContainer = document.getElementById('videoContainer');
            if (!videoContainer) {
                return;
            }
            
            videoContainer.innerHTML = '';
            
            if (!newVideos || newVideos.length !== 2) {
                videoContainer.innerHTML = '<div class="error">Greska pri ucitavanju novih videa</div>';
                return;
            }
            
            newVideos.forEach(function(video, index) {
                const safeTitle = video.title ? video.title.replace(/[&<>"']/g, function(m) {
                    const map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'};
                    return map[m];
                }) : '';
                
                const safeEmbedUrl = video.embedUrl ? video.embedUrl.replace(/[&<>"']/g, function(m) {
                    const map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'};
                    return map[m];
                }) : '';
                
                const videoHtml = 
                    '<div class="video-item" data-video-id="' + video.id + '">' +
                        '<h3>' + safeTitle + '</h3>' +
                        '<iframe width="560" height="315" ' +
                                'src="' + safeEmbedUrl + '" ' +
                                'title="YouTube video player" ' +
                                'frameborder="0" ' +
                                'allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" ' +
                                'referrerpolicy="strict-origin-when-cross-origin" ' +
                                'allowfullscreen>' +
                        '</iframe>' +
                        '<button type="button" class="vote-btn" onclick="voteForVideo(' + video.id + ')">' +
                            '🗳️ Vote for this video!' +
                        '</button>' +
                    '</div>';
                
                videoContainer.innerHTML += videoHtml;
            });
            
            updateShareUrl();
        }
        
        function showMessage(message, type) {
            const messageContainer = document.getElementById('messageContainer');
            if (!messageContainer) {
                return;
            }
            
            messageContainer.style.display = 'block';
            messageContainer.className = type + '-message';
            
            switch(type) {
                case 'success':
                    messageContainer.style.backgroundColor = '#d4edda';
                    messageContainer.style.color = '#155724';
                    messageContainer.style.border = '1px solid #c3e6cb';
                    break;
                case 'warning':
                    messageContainer.style.backgroundColor = '#fff3cd';
                    messageContainer.style.color = '#856404';
                    messageContainer.style.border = '1px solid #ffeaa7';
                    break;
                case 'error':
                default:
                    messageContainer.style.backgroundColor = '#f8d7da';
                    messageContainer.style.color = '#721c24';
                    messageContainer.style.border = '1px solid #f5c6cb';
                    break;
            }
            
            const prefix = type === 'success' ? 'Uspjeh:' : 
                           type === 'warning' ? 'Upozorenje:' : 'Greska:';
            
            const safeMessage = message ? message.replace(/[&<>"']/g, function(m) {
                const map = {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'};
                return map[m];
            }) : '';
            
            messageContainer.innerHTML ='</strong> ' + safeMessage;
            
            setTimeout(function() {
                if (messageContainer.style.display !== 'none') {
                    messageContainer.style.display = 'none';
                }
            }, 5000);
        }
        
        function toggleShareOptions() {
            const shareOptions = document.getElementById('shareOptions');
            
            if (!shareOptions) {
                return;
            }
            
            const isVisible = shareOptions.classList.contains('show');
            
            if (isVisible) {
                closeShareOptions();
            } else {
                shareOptions.classList.add('show');
                updateShareUrl();
            }
        }
        
        function closeShareOptions() {
            const shareOptions = document.getElementById('shareOptions');
            if (shareOptions) {
                shareOptions.classList.remove('show');
            }
        }
        
        function updateShareUrl() {
            const videoContainers = document.querySelectorAll('.video-item');
            
            if (videoContainers.length === 2) {
                const video1Id = videoContainers[0].getAttribute('data-video-id');
                const video2Id = videoContainers[1].getAttribute('data-video-id');
                
                // Kreiraj share URL sa video parametrima
                const shareUrl = window.location.origin + window.location.pathname + 
                                '?video1=' + video1Id + '&video2=' + video2Id;
                
                const shareUrlInput = document.getElementById('shareUrl');
                if (shareUrlInput) {
                    shareUrlInput.value = shareUrl;
                }
            }
        }
        
        function copyToClipboard() {
            const shareUrlInput = document.getElementById('shareUrl');
            const copyBtn = document.querySelector('.copy-btn');
            
            if (!shareUrlInput) {
                return;
            }
            
            try {
                shareUrlInput.select();
                shareUrlInput.setSelectionRange(0, 99999);
                
                if (navigator.clipboard && window.isSecureContext) {
                    navigator.clipboard.writeText(shareUrlInput.value).then(function() {
                        copySuccess(copyBtn);
                    }).catch(function(err) {
                        fallbackCopy(shareUrlInput, copyBtn);
                    });
                } else {
                    fallbackCopy(shareUrlInput, copyBtn);
                }
            } catch (err) {
                showMessage('Greska pri kopiranju linka', 'error');
            }
        }
        
        function fallbackCopy(input, btn) {
            try {
                const successful = document.execCommand('copy');
                if (successful) {
                    copySuccess(btn);
                } else {
                    showMessage('Kopiranje nije uspjesno', 'error');
                }
            } catch (err) {
                showMessage('Kopiranje nije podrzano u ovom browseru', 'error');
            }
        }
        
        function copySuccess(btn) {
            if (!btn) return;
            
            const originalText = btn.innerHTML;
            btn.innerHTML = '✅ Kopirano!';
            btn.classList.add('copied');
            
            showMessage('Link je kopiran u clipboard!', 'success');
            
            setTimeout(function() {
                btn.innerHTML = originalText;
                btn.classList.remove('copied');
            }, 2000);
        }
        
        function shareOnTwitter() {
            const shareUrlInput = document.getElementById('shareUrl');
            if (!shareUrlInput) {
                return;
            }
            
            const shareUrl = shareUrlInput.value;
            const text = encodeURIComponent('Pogledaj ove odlične YouTube videe i glasaj za svoj omiljeni! 🎵');
            const twitterUrl = 'https://twitter.com/intent/tweet?text=' + text + '&url=' + encodeURIComponent(shareUrl);
            
            window.open(twitterUrl, '_blank', 'width=600,height=400');
        }
        
        function shareOnFacebook() {
            const shareUrlInput = document.getElementById('shareUrl');
            if (!shareUrlInput) {
                return;
            }
            
            const shareUrl = shareUrlInput.value;
            const facebookUrl = 'https://www.facebook.com/sharer/sharer.php?u=' + encodeURIComponent(shareUrl);
            
            window.open(facebookUrl, '_blank', 'width=600,height=400');
        }
        
        function shareOnLinkedIn() {
            const shareUrlInput = document.getElementById('shareUrl');
            if (!shareUrlInput) {
                return;
            }
            
            const shareUrl = shareUrlInput.value;
            const linkedinUrl = 'https://www.linkedin.com/sharing/share-offsite/?url=' + encodeURIComponent(shareUrl);
            
            window.open(linkedinUrl, '_blank', 'width=600,height=400');
        }
        
        function shareOnWhatsApp() {
            const shareUrlInput = document.getElementById('shareUrl');
            if (!shareUrlInput) {
                return;
            }
            
            const shareUrl = shareUrlInput.value;
            const text = encodeURIComponent('Pogledaj ove YouTube videe i glasaj za svoj omiljeni! 🎵 ' + shareUrl);
            const whatsappUrl = 'https://wa.me/?text=' + text;
            
            window.open(whatsappUrl, '_blank');
        }
        
        document.addEventListener('DOMContentLoaded', function() {
            // Postavi share URL kada se stranica učita
            setTimeout(updateShareUrl, 500);
            
            // Ako je ovo dijeljeni link, prikaži poruku
            if (isSharedLink()) {
                showMessage('Prikazujem dijeljene videe! Kliknite "Učitaj nova random videa" za random par.', 'success');
            }
        });
    </script>
</body>
</html>