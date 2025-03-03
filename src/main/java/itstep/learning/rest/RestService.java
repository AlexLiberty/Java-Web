package itstep.learning.rest;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestService {
    public final Gson gson = new Gson();

    public void SendResponse(HttpServletResponse resp, RestResponse restResponse) throws IOException {
        resp.setContentType("application/json");
        setCorsHeaders(resp);
        resp.getWriter().print(gson.toJson(restResponse)
        );
    }

    public void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*"); //CORS http://localhost:5173
        resp.setHeader("Access-Control-Allow-Headers", "authorization, content-type"); //дозволити json
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
    }

    public <T> T fromJson(String json, Class<T> classOfT){
        return  gson.fromJson(json, classOfT);
    }

    public <T> T fromBody(HttpServletRequest req, Class<T> classOfT) throws IOException {
        String charsetName = req.getCharacterEncoding();
        if (charsetName == null)
        {
            charsetName = StandardCharsets.UTF_8.name();
        }
        return  gson.fromJson(new String(
                req.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8),classOfT);
    }
}
