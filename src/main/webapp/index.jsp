<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>MasterAnnonce - Menu</title>
</head>
<body>
<h1>Bienvenue ${sessionScope.username}</h1>

<ul>
    <li><a href="annonce-list">Liste paginée des annonces</a></li>
    <li><a href="annonce-add">Créer une annonce</a></li>
</ul>

<form method="post" action="logout">
    <button type="submit">Se déconnecter</button>
</form>
</body>
</html>