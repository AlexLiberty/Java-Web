package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mysql.cj.jdbc.Driver;
import itstep.learning.models.UserSignupFormModel;
import itstep.learning.rest.RestResponse;
//import jakarta.inject.Singleton;
import itstep.learning.services.random.RandomService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@WebServlet("/home")
@Singleton

public class HomeServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final RandomService randomService;

    @Inject
    public HomeServlet(RandomService randomService) {
        this.randomService = randomService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message;
        //підключення до бази даних
        try {
            DriverManager.registerDriver(
                    new Driver()
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

            resultSet = statement.executeQuery("SHOW DATABASES");
            StringBuilder sb = new StringBuilder();
            while (resultSet.next()){
                sb.append(", ");
                sb.append(resultSet.getString(1));
            }

            resultSet.close();
            statement.close();
            message += sb.toString();

        } catch (SQLException e) {
            message = e.getMessage();
        }

        sendJson(resp,
                new RestResponse()
                .setResourceUrl("POST /time")
                .setStatus(200)
                .setMessage(message + randomService.randomInt())
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String body = new String(req.getInputStream().readAllBytes());
        UserSignupFormModel model;
        RestResponse restResponse =
                new RestResponse()
                        .setResourceUrl("POST /home")
                        .setCacheTime(0);
        try
        {
            model = gson.fromJson(body, UserSignupFormModel.class);
        }
        catch (Exception ex)
        {
            sendJson(resp, restResponse
                    .setStatus(422)
                    .setMessage(ex.getMessage())
            );
            return;
        }
        sendJson(resp, restResponse
                        .setStatus(201)
                        .setMessage("Created")
                        .setMeta(Map.of(
                                "dataType", "object",
                                "read", "GET /home",
                                "update", "PUT /home",
                                "delete", "DELETE /home"
                        ))
                        .setDate(model)
    );
    }

    private void sendJson(HttpServletResponse resp, RestResponse restResponse) throws IOException {
        resp.setContentType("application/json");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); //CORS

        resp.getWriter().print(
                gson.toJson(restResponse)
        );
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); //CORS
        resp.setHeader("Access-Control-Allow-Headers", "content-type"); //дозволити json
    }
}
