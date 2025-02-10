package itstep.learning.servlets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.DataContext;
import itstep.learning.dal.dto.User;
import itstep.learning.models.UserSignupFormModel;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestService;
import itstep.learning.services.db.DbService;
import itstep.learning.services.kdf.KdfService;
import itstep.learning.services.random.RandomService;
import itstep.learning.services.time.TimeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.Map;

//@WebServlet("/home")
@Singleton

public class HomeServlet extends HttpServlet {
    private final RandomService randomService;
    private final KdfService kdfService;
    private final DbService dbService;
    private final TimeService timeService;
    private final DataContext dataContext;
    private final RestService restService;

    @Inject
    public HomeServlet(RandomService randomService, KdfService kdfService, DbService dbService, TimeService timeService, DataContext dataContext, RestService restService)
    {
        this.randomService = randomService;
        this.kdfService = kdfService;
        this.dbService = dbService;
        this.timeService = timeService;
        this.dataContext = dataContext;
        this.restService = restService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message;
        //підключення до бази даних
        try {

            String sql = "SELECT CURRENT_TIMESTAMP"; //запит
            Statement statement = dbService.getConnection().createStatement(); // інструмент передачі запиту у базу даних
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

        String msg = dataContext.getUserDao().installTables()
                ? "Install OK"
                : "Install Fail";

        restService.SendResponse(resp,
                new RestResponse()
                .setResourceUrl("POST /time")
                .setStatus(200)
                .setMessage(message + " Time " + timeService.getIsoTime() + " Random " + randomService.randomInt() + " Hash: " + kdfService.dk("123", "456") + " DATABASE: " + msg)
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String body = new String(req.getInputStream().readAllBytes());
        UserSignupFormModel model;
        RestResponse restResponse =
                new RestResponse()
                        .setResourceUrl("POST /home")
                        .setCacheTime(0)
                        .setMeta(Map.of(
                                "dataType", "object",
                                "read", "GET /home",
                                "update", "PUT /home",
                                "delete", "DELETE /home"
                        ));
        try
        {
            model = restService.fromJson(body, UserSignupFormModel.class);
        }
        catch (Exception ex)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(422)
                    .setMessage(ex.getMessage())
            );
            return;
        }

        User user = dataContext.getUserDao().addUser(model);
        if( user == null)
        {
            restResponse
                    .setStatus(507)
                    .setMessage("DB Error")
                    .setDate(model);
        }
        else
        {
            restResponse
                    .setStatus(201)
                    .setMessage("Created")
                    .setDate(model);
        }

        restService.SendResponse(resp, restResponse);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
      restService.setCorsHeaders(resp);
    }
}
