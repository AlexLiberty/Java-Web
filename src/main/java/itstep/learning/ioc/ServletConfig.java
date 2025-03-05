package itstep.learning.ioc;

import com.google.inject.servlet.ServletModule;
import itstep.learning.filter.AuthFilter;
import itstep.learning.filter.AuthJwtFilter;
import itstep.learning.filter.CharsetFilter;
import itstep.learning.servlets.*;

public class ServletConfig extends ServletModule {
    @Override
    protected void configureServlets() {
       // !! Для усіх сервлетів у проекті ми прибираємо анотацію @WebServlet та додаємо анотацію @ Singleton
    filter("/*").through(CharsetFilter.class);
    //filter("/*").through(AuthFilter.class);
        filter("/*").through(AuthJwtFilter.class);
    serve("/home").with(HomeServlet.class);
    serve("/time").with(TimeServlet.class);
    serve("/user").with(UserServlet.class);
    serve("/random").with(RandomServlet.class);
    serve("/product").with(ProductServlet.class);
    serve("/storage/*").with(StorageServlet.class);
    }
}
