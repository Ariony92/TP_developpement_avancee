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
<c:if test="${not empty errors.global}">
    <p style="color:red;">${errors.global}</p>
</c:if>

<form method="post" action="annonce-add">
    <p>
        Title :
        <input type="text" name="title" value="${titleValue}" required>
        <c:if test="${not empty errors.title}"><span style="color:red;"> ${errors.title}</span></c:if>
    </p>

    <p>
        Description :
        <textarea name="description" required>${descriptionValue}</textarea>
        <c:if test="${not empty errors.description}"><span style="color:red;"> ${errors.description}</span></c:if>
    </p>

    <p>
        Address :
        <input type="text" name="adress" value="${adressValue}" required>
        <c:if test="${not empty errors.adress}"><span style="color:red;"> ${errors.adress}</span></c:if>
    </p>

    <p>
        Mail :
        <input type="email" name="mail" value="${mailValue}" required>
        <c:if test="${not empty errors.mail}"><span style="color:red;"> ${errors.mail}</span></c:if>
    </p>
    <p>
        Catégorie :
        <select name="categoryId" required>
            <option value="">-- Choisir --</option>
            <c:forEach var="c" items="${categories}">
                <option value="${c.id}" ${categoryIdValue == c.id ? 'selected' : ''}>${c.label}</option>
            </c:forEach>
        </select>
        <c:if test="${not empty errors.categoryId}"><span style="color:red;"> ${errors.categoryId}</span></c:if>
    </p>

    <button type="submit">Enregistrer</button>
</form>
<p>
    <a href="annonce-list">Retour liste</a> |
    <a href="index.jsp">Menu</a>
</p>
</body>
</html>

