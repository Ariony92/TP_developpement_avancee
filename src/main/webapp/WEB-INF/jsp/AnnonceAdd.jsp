<%--
  Created by IntelliJ IDEA.
  User: zkhan
  Date: 29/01/2026
  Time: 15:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Ajouter une annonce</title>
</head>
<body>

<h1>Ajouter une annonce</h1>

<form method="post" action="annonce-add">
    <p>Title : <input type="text" name="title" required></p>
    <p>Description : <textarea name="description" required></textarea></p>
    <p>Adress : <input type="text" name="adress" required></p>
    <p>Mail : <input type="text" name="mail" required></p>
    <button type="submit">Enregistrer</button>
</form>
</body>
</html>

