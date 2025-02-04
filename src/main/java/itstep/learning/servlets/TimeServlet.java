package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.rest.RestResponse;
import itstep.learning.services.time.TimeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class TimeServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final TimeService timeService;

    @Inject
    public TimeServlet(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().print(
                gson.toJson(
                        new RestResponse()
                                .setStatus(200)
                                .setMessage("Timestamp: " + timeService.getTimestamp() + ", ISO: " + timeService.getIsoTime())
                )
        );
    }
}
