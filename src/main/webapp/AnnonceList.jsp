<%--
  Created by IntelliJ IDEA.
  User: zkhan
  Date: 29/01/2026
  Time: 15:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Liste des annonces</title>
</head>
<body>

<h1>Liste des annonces</h1>

<p>
    <a href="annonce-add">Ajouter une annonce</a> |
    <a href="index.jsp">Menu</a>
</p>

<c:choose>
    <c:when test="${empty annonces}">
        <p>Aucune annonce.</p>
    </c:when>

    <c:otherwise>
        <table border="1" cellpadding="6">
            <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Adress</th>
                <th>Mail</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>

            <c:forEach var="a" items="${annonces}">
                <tr>
                    <td>${a.id}</td>
                    <td>${a.title}</td>
                    <td>${a.adress}</td>
                    <td>${a.mail}</td>
                    <td>${a.date}</td>
                    <td>
                        <c:url var="updateUrl" value="annonce-update">
                            <c:param name="id" value="${a.id}" />
                        </c:url>

                        <c:url var="deleteUrl" value="annonce-delete">
                            <c:param name="id" value="${a.id}" />
                        </c:url>

                        <a href="${updateUrl}">Modifier</a>
                        |
                        <a href="${deleteUrl}" onclick="return confirm('Supprimer cette annonce ?');">
                            Supprimer
                        </a>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>
