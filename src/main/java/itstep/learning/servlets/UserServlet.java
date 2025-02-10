package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.DataContext;
import itstep.learning.rest.RestResponse;
import itstep.learning.rest.RestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@Singleton
public class UserServlet extends HttpServlet {
    private final DataContext dataContext;
    private final RestService restService;

@Inject
    public UserServlet(DataContext dataContext, RestService restService) {
        this.dataContext = dataContext;
        this.restService = restService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestResponse restResponse =
                new RestResponse()
                        .setResourceUrl("GET /user")
                        .setCacheTime(600)
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

        restResponse.setDate(credentials);
    restService.SendResponse(resp, restResponse);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        restService.setCorsHeaders(resp);
    }
}
