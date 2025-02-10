package itstep.learning.ioc;

import com.google.inject.servlet.ServletModule;
import itstep.learning.servlets.HomeServlet;
import itstep.learning.servlets.TimeServlet;
import itstep.learning.servlets.UserServlet;

public class ServletConfig extends ServletModule {
    @Override
    protected void configureServlets() {
       // !! Для усіх сервлетів у проекті ми прибираємо анотацію @WebServlet та додаємо анотацію @ Singleton
    serve("/home").with(HomeServlet.class);
    serve("/time").with(TimeServlet.class);
    serve("/user").with(UserServlet.class);
    }
}
