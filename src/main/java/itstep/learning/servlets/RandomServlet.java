package itstep.learning.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.services.random.RandomService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class RandomServlet extends HttpServlet {
    private final RandomService randomService;

@Inject
    public RandomServlet(RandomService randomService) {
        this.randomService = randomService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = req.getParameter("type");
        String lengthParam = req.getParameter("length");

        int length;
        try {
            length = Integer.parseInt(lengthParam);
            if (length <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid length parameter");
            return;
        }

        String generatedValue;
        switch (type.toLowerCase()) {
            case "string":
                generatedValue = randomService.randomString(length);
                break;
            case "filename":
                generatedValue = randomService.randomFileName(length);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid type parameter");
                return;
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("resourceUrl", "GET /random");

        Map<String, Object> meta = new HashMap<>();
        meta.put("dataType", "string");
        meta.put("read", "GET /random");
        meta.put("type", type);
        meta.put("length", length);

        response.put("meta", meta);
        response.put("cacheTime", 0);
        response.put("data", generatedValue);

        resp.getWriter().write(toJson(response));
    }

    private String toJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\",");
            } else {
                json.append(entry.getValue()).append(",");
            }
        }
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
}
