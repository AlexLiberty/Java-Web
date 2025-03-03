package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.DataContext;
import itstep.learning.dal.dto.AccessToken;
import itstep.learning.dal.dto.User;
import itstep.learning.dal.dto.UserAccess;
import itstep.learning.models.UserAuthJwtModel;
import itstep.learning.models.UserAuthModel;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestService;
import itstep.learning.services.hash.HashService;
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
    private final HashService hashService;

@Inject
    public UserServlet(DataContext dataContext, RestService restService, Logger logger, HashService hashService) {
        this.dataContext = dataContext;
        this.restService = restService;
        this.logger = logger;
    this.hashService = hashService;
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
        UserAccess userAccess = dataContext.getUserDao().authorize(parts [0], parts[1]);
        if(userAccess == null)
        {
            restService.SendResponse(resp, restResponse
                    .setStatus(401)
                    .setDate("Credentials rejeckted"));
            return;
        }

        AccessToken token = dataContext.getAccessTokenDao().create(userAccess);
        User user = dataContext.getUserDao().getUserById(userAccess.getUserId());

        String jwtHeader = new String(Base64.getUrlEncoder()
                .encode("{\"alg\": \"HS256\", \"typ\": \"JWT\" }".getBytes()));
        String jwtPayload = new String(Base64.getUrlEncoder()
                .encode( restService.gson.toJson(userAccess).getBytes()));
        String jwtSignature = new String(Base64.getUrlEncoder()
                .encode(hashService
                        .digest("secret" + jwtHeader + "." + jwtPayload).getBytes()));

        String jwtToken = jwtHeader + "." + jwtPayload + "." + jwtSignature;

        restResponse.setStatus(200)
                //.setDate(new UserAuthModel(user, userAccess, token)) //як звичайний токен
                .setDate(new UserAuthJwtModel(user, jwtToken)) // як JWT токен
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

        // Перевіряємо авторизацію за токеном
        UserAccess userAccess = (UserAccess) req.getAttribute("authUserAccess");

        if( userAccess == null ) {
            restService.SendResponse( resp,
                    restResponse.setStatus( 401 )
                            .setDate(req.getAttribute("authStatus")) );
            return;
        }

        User userUpdates;

        try {
            userUpdates = restService.fromBody(req, User.class);
        } catch (Exception ex) {
            restService.SendResponse(resp, restResponse
                    .setStatus(422)
                    .setMessage(ex.getMessage())
            );
            return;
        }

        if (userUpdates == null || userUpdates.getUserId() == null) {
            restService.SendResponse(resp, restResponse
                    .setStatus(422)
                    .setMessage("Unparseable data or identity undefined"));
            return;
        }

        User user = dataContext.getUserDao().getUserById(userUpdates.getUserId());
        if (user == null) {
            restService.SendResponse(resp, restResponse
                    .setStatus(404)
                    .setMessage("User not found"));
            return;
        }

        try {
            dataContext.getUserDao().updateAsync(userUpdates).get();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "updateAsync fail: {0}", ex.getMessage());
            restService.SendResponse(resp, restResponse
                    .setStatus(500)
                    .setMessage("Server error. See server's logs"));
            return;
        }

        restResponse
                .setStatus(202)
                .setDate(userUpdates)
                .setCacheTime(0);
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

