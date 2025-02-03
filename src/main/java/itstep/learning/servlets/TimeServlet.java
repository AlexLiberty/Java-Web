package itstep.learning.servlets;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import itstep.learning.rest.RestResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

//@WebServlet("/time")
@Singleton

public class TimeServlet extends HttpServlet {
    private final Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long timestamp = System.currentTimeMillis();

        String isoTime = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        resp.getWriter().print(
                gson.toJson(
                        new RestResponse()
                                .setStatus(200)
                                .setMessage("Timestamp: " + timestamp + ", ISO: " + isoTime)
                )
        );
    }
}
