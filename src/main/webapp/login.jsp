<%--
  Created by IntelliJ IDEA.
  User: Kf51A
  Date: 11/02/2026
  Time: 21:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Connexion</title>
</head>
<body>
<h1>Connexion</h1>

<c:if test="${not empty errors.global}">
    <p style="color:red;">${errors.global}</p>
</c:if>

<form method="post" action="login">
    <p>
        Login (username ou email) :
        <input type="text" name="login" value="${loginValue}" required>
        <c:if test="${not empty errors.login}"><span style="color:red;"> ${errors.login}</span></c:if>
    </p>
    <p>
        Mot de passe :
        <input type="password" name="password" required>
        <c:if test="${not empty errors.password}"><span style="color:red;"> ${errors.password}</span></c:if>
    </p>
    <button type="submit">Se connecter</button>
</form>

<p>Utilise un compte déjà présent en base (`users`).</p>
</body>
</html>
