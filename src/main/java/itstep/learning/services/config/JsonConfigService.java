package itstep.learning.services.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class JsonConfigService implements ConfigService
{
    private JsonElement configElement;
    private final Logger logger;

    @Inject
    public JsonConfigService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public JsonPrimitive getValue(String path)
    {
        if(configElement == null)
        {
            init();
        }

        String[] parts = path.split("\\.");
        JsonObject obj = configElement.getAsJsonObject();

        for (int i =0; i<parts.length - 1; ++i)
        {
            JsonElement je = obj.get(parts[i]);
            if(je == null)
            {
                throw new NoSuchElementException(
                        "Part of path '" +parts[i]+ "' not found");
            }

           obj = je.getAsJsonObject();
        }
        return obj.getAsJsonPrimitive(parts[parts.length - 1]);
    }

    private void init()
    {
        try(Reader reader = new InputStreamReader(
                this.getClass()
                        .getClassLoader()
                .getResourceAsStream ("appsettings.json")))
        {
            configElement = new Gson().fromJson(reader, JsonElement.class);
        }
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, "JsonConfigService::init", ex);
        }
    }
}
