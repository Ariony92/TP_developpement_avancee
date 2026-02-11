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
<c:set var="currentId" value="${empty formId ? annonce.id : formId}"/>
<c:set var="currentTitle" value="${empty titleValue ? annonce.title : titleValue}"/>
<c:set var="currentDescription" value="${empty descriptionValue ? annonce.description : descriptionValue}"/>
<c:set var="currentAdress" value="${empty adressValue ? annonce.adress : adressValue}"/>
<c:set var="currentMail" value="${empty mailValue ? annonce.mail : mailValue}"/>
<c:set var="currentCategoryId" value="${empty categoryIdValue ? annonce.category.id : categoryIdValue}"/>

<h1>Modifier annonce #${currentId}</h1>

<c:if test="${not empty errors.global}">
    <p style="color:red;">${errors.global}</p>
</c:if>

<form method="post" action="annonce-update">
    <input type="hidden" name="id" value="${currentId}">

    <p>
        Title :
        <input type="text" name="title" value="${currentTitle}" required>
        <c:if test="${not empty errors.title}"><span style="color:red;"> ${errors.title}</span></c:if>
    </p>

    <p>
        Description :
        <textarea name="description" required>${currentDescription}</textarea>
        <c:if test="${not empty errors.description}"><span style="color:red;"> ${errors.description}</span></c:if>
    </p>

    <p>
        Address :
        <input type="text" name="adress" value="${currentAdress}" required>
        <c:if test="${not empty errors.adress}"><span style="color:red;"> ${errors.adress}</span></c:if>
    </p>

    <p>
        Mail :
        <input type="email" name="mail" value="${currentMail}" required>
        <c:if test="${not empty errors.mail}"><span style="color:red;"> ${errors.mail}</span></c:if>
    </p>
    <p>
        Catégorie :
        <select name="categoryId" required>
            <c:forEach var="c" items="${categories}">
                <option value="${c.id}" ${currentCategoryId == c.id ? 'selected' : ''}>${c.label}</option>
            </c:forEach>
        </select>
        <c:if test="${not empty errors.categoryId}"><span style="color:red;"> ${errors.categoryId}</span></c:if>
    </p>

    <button type="submit">Enregistrer</button>
</form>

<p>
    <a href="annonce-detail?id=${currentId}">Voir le détail</a> |
    <a href="annonce-list">Retour liste</a>
</p>

</body>
</html>
