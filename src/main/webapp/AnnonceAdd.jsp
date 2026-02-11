<%--
  Created by IntelliJ IDEA.
  User: zkhan
  Date: 29/01/2026
  Time: 15:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Créer une annonce</title>
</head>
<body>

<h1>Créer une annonce</h1>

<c:if test="${not empty error}">
    <p style="color:red;">${error}</p>
</c:if>
<form method="post" action="annonce-add">
    <p>Title : <input type="text" name="title" required></p>
    <p>Description : <textarea name="description" required></textarea></p>
    <p>Address : <input type="text" name="adress" required></p>
    <p>Mail : <input type="email" name="mail" required></p>

    <p>
        Catégorie :
        <select name="categoryId" required>
            <option value="">-- Choisir --</option>
            <c:forEach var="c" items="${categories}">
                <option value="${c.id}">${c.label}</option>
            </c:forEach>
        </select>
    </p>

    <button type="submit">Enregistrer</button>
</form>
<p>
    <a href="annonce-list">Retour liste</a> |
    <a href="index.jsp">Menu</a>
</p>
</body>
</html>

