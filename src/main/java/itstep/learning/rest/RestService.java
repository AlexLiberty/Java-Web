package itstep.learning.rest;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RestService {
    private final Gson gson = new Gson();

    public void SendResponse(HttpServletResponse resp, RestResponse restResponse) throws IOException {
        resp.setContentType("application/json");
        setCorsHeaders(resp);
        resp.getWriter().print(gson.toJson(restResponse)
        );
    }

    public void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*"); //CORS http://localhost:5174
        resp.setHeader("Access-Control-Allow-Headers", "authorization, content-type"); //дозволити json
    }

    public <T> T fromJson(String json, Class<T> classOfT){
        return  gson.fromJson(json, classOfT);
    }
}
