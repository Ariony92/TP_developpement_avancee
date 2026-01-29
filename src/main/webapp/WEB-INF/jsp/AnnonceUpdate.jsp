<%--
  Created by IntelliJ IDEA.
  User: zkhan
  Date: 29/01/2026
  Time: 15:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Modifier annonce</title>
</head>
<body>

<h1>Modifier annonce #${annonce.id}</h1>

<form method="post" action="annonce-update">
    <input type="hidden" name="id" value="${annonce.id}">

    <p>Title : <input type="text" name="title" value="${annonce.title}" required></p>
    <p>Description : <textarea name="description" required>${annonce.description}</textarea></p>
    <p>Adress : <input type="text" name="adress" value="${annonce.adress}" required></p>
    <p>Mail : <input type="email" name="mail" value="${annonce.mail}" required></p>

    <button type="submit">Enregistrer</button>
</form>

<p><a href="annonce-list">Retour liste</a></p>

</body>
</html>
