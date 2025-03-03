package itstep.learning.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import itstep.learning.dal.dao.DataContext;
import itstep.learning.dal.dto.UserAccess;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@Singleton
public class AuthFilter implements Filter
{
    private FilterConfig filterConfig;
    private final DataContext  dataContext;

    @Inject
    public AuthFilter(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse resp, FilterChain next)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) sreq;
        String authStatus;
        // Перевіряємо авторизацію за токеном
        String authHeader = req.getHeader( "Authorization" );
        if( authHeader == null )
        {
            authStatus = "Authorization header required";
        }
        else
        {
            String authScheme = "Bearer ";
            if (!authHeader.startsWith(authScheme)) {
                authStatus = "Authorization scheme error";
            } else {
                String credentials = authHeader.substring(authScheme.length());
                UserAccess userAccess = dataContext
                        .getAccessTokenDao()
                        .getUserAccess(credentials);
                if (userAccess == null) {
                    authStatus = "Token expires or invalid";
                } else {
                    authStatus = "OK";
                    req.setAttribute("authUserAccess", userAccess);
                }
            }
        }
        req.setAttribute("authStatus", authStatus);
        next.doFilter( req, resp);
    }

    @Override
    public void destroy()
    {
        this.filterConfig = null;
    }
}
