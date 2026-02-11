<%--
  Created by IntelliJ IDEA.
  User: zkhan
  Date: 29/01/2026
  Time: 15:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Modifier annonce</title>
</head>
<body>
<h1>Modifier annonce #${annonce.id}</h1>
<c:if test="${not empty error}">
    <p style="color:red;">${error}</p>
</c:if>

<form method="post" action="annonce-update">
    <input type="hidden" name="id" value="${annonce.id}">

    <p>Title : <input type="text" name="title" value="${annonce.title}" required></p>
    <p>Description : <textarea name="description" required>${annonce.description}</textarea></p>
    <p>Address : <input type="text" name="adress" value="${annonce.adress}" required></p>
    <p>Mail : <input type="email" name="mail" value="${annonce.mail}" required></p>

    <p>
        Catégorie :
        <select name="categoryId" required>
            <c:forEach var="c" items="${categories}">
                <option value="${c.id}" ${annonce.category.id == c.id ? 'selected' : ''}>${c.label}</option>
            </c:forEach>
        </select>
    </p>

    <button type="submit">Enregistrer</button>
</form>

<p>
    <a href="annonce-detail?id=${annonce.id}">Voir le détail</a> |
    <a href="annonce-list">Retour liste</a>
</p>

</body>
</html>
