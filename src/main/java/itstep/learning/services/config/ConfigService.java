package itstep.learning.services.config;

import com.google.gson.JsonPrimitive;

public interface ConfigService
{
    JsonPrimitive getValue(String path);
}
