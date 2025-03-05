<%@ page contentType="text/html; charset=UTF8" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<a href = "home">Home</a>
<a href = "time">Time</a>
<a href = "user">User</a>
<a href = "random?type=string&length=10">Random</a>
<a href = "product">Product</a>
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
<br><br><br><br>
<form action="product" method="post" enctype="multipart/form-data">
    <input name="field1" value="value 1"/>
    <input name="field2" value="value 2"/>
    <input type="file" name="file1"/>
    <button>send</button>
</form>
</body>
</html>
