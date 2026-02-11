<%--
  Created by IntelliJ IDEA.
  User: Kf51A
  Date: 11/02/2026
  Time: 21:26
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Détail annonce</title>
</head>
<body>

<h1>Détail annonce #${annonce.id}</h1>

<ul>
    <li><strong>Title :</strong> ${annonce.title}</li>
    <li><strong>Description :</strong> ${annonce.description}</li>
    <li><strong>Address :</strong> ${annonce.adress}</li>
    <li><strong>Mail :</strong> ${annonce.mail}</li>
    <li><strong>Date :</strong> ${annonce.date}</li>
    <li><strong>Status :</strong> ${annonce.status}</li>
    <li><strong>Category :</strong> ${annonce.category.label}</li>
    <li><strong>Author :</strong> ${annonce.author.username}</li>
</ul>

<p>
    <a href="annonce-update?id=${annonce.id}">Modifier</a> |
    <a href="annonce-list">Retour liste</a>
</p>

</body>
</html>
