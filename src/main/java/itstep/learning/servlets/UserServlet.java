package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.DataContext;
import itstep.learning.dal.dto.User;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class UserServlet extends HttpServlet {
    private final DataContext dataContext;
    private final RestService restService;
    private final Logger logger;

@Inject
    public UserServlet(DataContext dataContext, RestService restService, Logger logger) {
        this.dataContext = dataContext;
        this.restService = restService;
        this.logger = logger;
}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestResponse restResponse =
                new RestResponse()
                        .setResourceUrl("GET /user")
                        .setMeta(Map.of(
                                "dataType", "object",
                                "read", "GET /user",
                                "update", "PUT /user",
                                "delete", "DELETE /user"
                        ));
        String authHeader = req.getHeader("Authorization");
        if(authHeader == null)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(401)
                    .setDate("Authorization header required"));
            return;
        }

        String autScheme = "Basic ";

        if(!authHeader.startsWith(autScheme))
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(401)
                    .setDate("Authorization scheme error"));
            return;
        }

        String credentials = authHeader.substring(autScheme.length());
        try
        {
            credentials = new String( Base64.getDecoder()
                    .decode(credentials.getBytes()));
        }
        catch (Exception ex){
            restService.SendResponse(resp, restResponse
                    .setStatus(422)
                    .setDate("Decode error" + ex.getMessage()));
            return;
        }

        String[] parts = credentials.split(":", 2);

        if(parts.length !=2)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(422)
                    .setDate("Format error splytting by"));
            return;
        }
        User user = dataContext.getUserDao().authorize(parts [0], parts[1]);
        if(user == null)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(401)
                    .setDate("Credentials rejeckted"));
            return;
        }

        restResponse.setStatus(200)
                .setDate(user)
                .setCacheTime(600);
        restService.SendResponse(resp, restResponse);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        RestResponse restResponse =
                new RestResponse()
                        .setResourceUrl("PUT /user")
                        .setCacheTime(0)
                        .setMeta(Map.of(
                                "dataType", "object",
                                "read", "GET /user",
                                "update", "PUT /user",
                                "delete", "DELETE /user"
                        ));

        User userUpdates;

        try
        {
            userUpdates = restService.fromBody(req, User.class);
        }
        catch (Exception ex)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(422)
                    .setMessage(ex.getMessage())
            );

            return;
        }

        if(userUpdates == null || userUpdates.getUserId() == null)
        {
            restResponse
                    .setStatus(422)
                    .setMessage("Unparseable data or identity undefined");
            return;
        }

        User user = dataContext
                .getUserDao()
                .getUserById(userUpdates.getUserId());
        if(user == null)
        {
            restResponse
                    .setStatus(404)
                    .setMessage("User not found");
            return;
        }

        if (!dataContext
                .getUserDao()
                .update(userUpdates)){
            restResponse
                    .setStatus(500)
                    .setMessage("Server error");
            return;
        }

        restResponse
                .setStatus(202)
                .setDate(userUpdates)
                .setCacheTime(0);;
        restService.SendResponse(resp, restResponse);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    RestResponse restResponse =
                new RestResponse()
                        .setResourceUrl("DELETE /user")
                        .setCacheTime(0)
                        .setMeta(Map.of(
                                "dataType", "object",
                                "read", "GET /user",
                                "update", "PUT /user",
                                "delete", "DELETE /user"
                        ));
    String userId = req.getParameter("id"); //user?id=...
        if(userId == null)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(400)
                    .setDate("Missing required ID"));
        }
        UUID userUuid;
        try
        {
            userUuid = UUID.fromString(userId);
        }
        catch (Exception ignore)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(400)
                    .setDate("Invalid Id format"));
            return;
        }

        User user = dataContext.getUserDao().getUserById(userUuid);

        if(user == null)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(401)
                    .setDate("Unauthorized"));
            return;
        }
        try
        {
            dataContext.getUserDao().deleteAsync(user).get();
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "deleteAsync fail: {0}", ex.getMessage());
            restService.SendResponse(resp, restResponse
                    .setStatus(500)
                    .setDate("Server error. See server's logs"));
            return;
        }

        restResponse
                .setStatus(202)
                .setDate("Deleted")
                .setCacheTime(0);
        restService.SendResponse(resp, restResponse);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        restService.setCorsHeaders(resp);
    }
}

