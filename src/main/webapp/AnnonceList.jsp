<%--
  Created by IntelliJ IDEA.
  User: zkhan
  Date: 29/01/2026
  Time: 15:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Liste des annonces</title>
</head>
<body>

<h1>Liste des annonces</h1>

<p>
    Connecté en tant que <strong>${sessionScope.username}</strong>
</p>
<p>
    <a href="annonce-add">Créer une annonce</a> |
    <a href="index.jsp">Menu</a>
</p>
<form method="get" action="annonce-list">
    <p>
        Mot-clé : <input type="text" name="q" value="${q}">
        Catégorie :
        <select name="categoryId">
            <option value="">-- toutes --</option>
            <c:forEach var="c" items="${categories}">
                <option value="${c.id}" ${categoryId == c.id ? 'selected' : ''}>${c.label}</option>
            </c:forEach>
        </select>
        Statut :
        <select name="status">
            <option value="">-- tous --</option>
            <c:forEach var="s" items="${statuses}">
                <option value="${s}" ${selectedStatus == s.name() ? 'selected' : ''}>${s}</option>
            </c:forEach>
        </select>
        <input type="hidden" name="size" value="${size}">
        <button type="submit">Filtrer</button>
    </p>
</form>

<c:choose>
    <c:when test="${empty annonces}">
        <p>Aucune annonce.</p>
    </c:when>

    <c:otherwise>
        <table border="1" cellpadding="6">
            <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Address</th>
                <th>Mail</th>
                <th>Status</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>

            <c:forEach var="a" items="${annonces}">
                <tr>
                    <td>${a.id}</td>
                    <td>${a.title}</td>
                    <td>${a.adress}</td>
                    <td>${a.mail}</td>
                    <td>${a.status}</td>
                    <td>${a.date}</td>
                    <td>
                        <a href="annonce-detail?id=${a.id}">Détail</a> |
                        <a href="annonce-update?id=${a.id}">Modifier</a>

                        <form method="post" action="annonce-delete" style="display:inline;">
                            <input type="hidden" name="id" value="${a.id}">
                            <button type="submit" onclick="return confirm('Supprimer cette annonce ?');">Supprimer</button>
                        </form>

                        <c:if test="${a.status == 'DRAFT'}">
                            <form method="post" action="annonce-status" style="display:inline;">
                                <input type="hidden" name="id" value="${a.id}">
                                <input type="hidden" name="action" value="publish">
                                <button type="submit">Publier</button>
                            </form>
                        </c:if>

                        <c:if test="${a.status == 'PUBLISHED'}">
                            <form method="post" action="annonce-status" style="display:inline;">
                                <input type="hidden" name="id" value="${a.id}">
                                <input type="hidden" name="action" value="archive">
                                <button type="submit">Archiver</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <p>
            <c:if test="${page > 1}">
                <a href="annonce-list?page=${page-1}&size=${size}&q=${q}&categoryId=${categoryId}&status=${selectedStatus}">Précédent</a>
            </c:if>
            <strong> Page ${page} </strong>
            <c:if test="${hasNext}">
                <a href="annonce-list?page=${page+1}&size=${size}&q=${q}&categoryId=${categoryId}&status=${selectedStatus}">Suivant</a>
            </c:if>
        </p>


    </c:otherwise>
</c:choose>
<form method="post" action="logout">
    <button type="submit">Se déconnecter</button>
</form>
</body>
</html>
