<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="bs">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Greska - YouTube Voting</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .error-container {
            text-align: center;
            margin: 50px auto;
            max-width: 600px;
            padding: 20px;
            border: 2px solid #dc3545;
            border-radius: 10px;
            background-color: #f8d7da;
        }
        .error-title {
            color: #721c24;
            font-size: 2em;
            margin-bottom: 20px;
        }
        .error-message {
            color: #721c24;
            font-size: 1.2em;
            margin-bottom: 30px;
        }
        .back-button {
            padding: 10px 20px;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            display: inline-block;
        }
        .back-button:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
    <header>
        <h1>YouTube Voting</h1>
        <nav>
            <ul>
                <li><a href="home">Vote</a></li>
                <li><a href="rankings.html">Rankings</a></li>
            </ul>
        </nav>
    </header>
    <main>
        <div class="error-container">
            <h2 class="error-title">🚫 Dogodila se greška!</h2>
            
            <c:if test="${not empty error}">
                <p class="error-message">${error}</p>
            </c:if>
            
            <c:if test="${empty error}">
                <p class="error-message">Dogodila se neocekivana greska. Molimo pokusajte ponovo.</p>
            </c:if>
            
            <div>
                <a href="home" class="back-button">🏠 Povratak na početnu</a>
            </div>
        </div>
    </main>
</body>
</html>