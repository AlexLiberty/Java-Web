package itstep.learning.filter;
import com.google.inject.Singleton;
import jakarta.servlet.*;
import java.io.IOException;

@Singleton
public class CharsetFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain next) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        next.doFilter(req, resp);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
//Фільтри (сервлетні фільтри) - це класи, що грають роль Middleware
