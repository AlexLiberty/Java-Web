<%@ page contentType="text/html; charset=UTF8" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h1>JSP</h1>
<h2>Вирази</h2>
<%= 2 + 3 %>
<h2>Змінні</h2>
<% int x = 10; %>
<%= x %>
<h2>Інструкції розгалуження</h2>>
<% if(x % 2 == 0) { %>
<b> Число <%= x %> парне</b>
<% }
else {%>
<i>Число <%= x %> не парне</i>
<%} %>
<ul>
<% for (int i = 0; i<10; i++){
%><li><%=i + 1 %></li>
    <%
}%>
    </ul>
</body>
</html>
