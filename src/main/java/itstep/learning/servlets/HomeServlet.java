package itstep.learning.servlets;

import com.google.gson.Gson;
import itstep.learning.rest.RestResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/home")

public class HomeServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message;
        //підключення до бази даних
        try {
            DriverManager.registerDriver(
                    new com.mysql.cj.jdbc.Driver()
            );
            String connectionString = "jdbc:mysql://localhost:3308/javaDb";
            Connection connection = DriverManager.getConnection(
                    connectionString,
                    "user1",
                    "pass123"
            );

            String sql = "SELECT CURRENT_TIMESTAMP"; //запит
            Statement statement = connection.createStatement(); // інструмент передачі запиту у базу даних
            ResultSet resultSet = statement.executeQuery(sql); // виконання запиту, використовуючи різні інструменти
            resultSet.next(); //

            message = resultSet.getString(1);// !!! JDBC відлік з 1

        } catch (SQLException e) {
            message = e.getMessage();
        }

        // Підключення до бази даних
        try {
            DriverManager.registerDriver(
                    new com.mysql.cj.jdbc.Driver()
            );
            String connectionString = "jdbc:mysql://localhost:3308/javaDb";
            Connection connection = DriverManager.getConnection(
                    connectionString,
                    "user1",
                    "pass123"
            );

            String sql = "SHOW DATABASES"; // Запит на отримання списку баз даних
            Statement statement = connection.createStatement(); // Інструмент для виконання запиту
            ResultSet resultSet = statement.executeQuery(sql); // Виконання запиту

            List<String> databases = new ArrayList<>();
            while (resultSet.next()) {
                databases.add(resultSet.getString(1));
            }

            message = String.join(", ", databases);

        } catch (SQLException e) {
            message = e.getMessage();
        }

        resp.setContentType("application/json");
        resp.getWriter().print(
                gson.toJson(
                        new RestResponse()
                .setStatus(200)
                .setMessage(message)
                )
        );
    }
}
