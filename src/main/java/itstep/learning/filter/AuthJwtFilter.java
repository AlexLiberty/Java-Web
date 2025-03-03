package itstep.learning.filter;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import itstep.learning.dal.dto.UserAccess;
import itstep.learning.services.hash.HashService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

@Singleton
public class AuthJwtFilter implements Filter {
    private FilterConfig filterConfig;
    private final HashService hashService;

    @Inject
    public AuthJwtFilter( HashService hashService ) {
        this.hashService = hashService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sresp,
                         FilterChain next) throws IOException, ServletException {

        checkJwt( (HttpServletRequest) sreq ) ;

//                UserAccess userAccess = null ;
//                if( userAccess == null ) {
//                    authStatus = "Token expires or invalid" ;
//                }
//                else {
//                    authStatus = "OK" ;
//                    req.setAttribute( "authUserAccess", userAccess );
//                }

        next.doFilter( sreq, sresp );
    }

    private void checkJwt( HttpServletRequest req ) {
        String secret = "secret";
        // Перевіряємо авторизацію за токеном
        String authHeader = req.getHeader( "Authorization" );
        if( authHeader == null ) {
            req.setAttribute( "authStatus", "Authorization header required" );
            return ;
        }
        String authScheme = "Bearer ";
        if( ! authHeader.startsWith( authScheme ) ) {
            req.setAttribute( "authStatus", "Authorization scheme error" );
            return ;
        }
        String credentials = authHeader.substring( authScheme.length() ) ;
        String[] parts = credentials.split("\\.");
        if( parts.length != 3 ) {
            req.setAttribute( "authStatus", "Token format invalid" );
            return ;
        }
        String header = parts[0];
        String payload = parts[1];
        String signature = new String(Base64.getUrlDecoder().decode(parts[2]));
        if(!signature.equals(hashService.digest(secret + header + "." + payload)))
        {
            req.setAttribute("authStatus", "Token signature error");
            return;
        }
        payload = new String(Base64.getUrlDecoder().decode(payload));
        UserAccess userAccess = new Gson().fromJson(payload, UserAccess.class);
        req.setAttribute("authStatus", "OK");
        req.setAttribute("authUserAccess", userAccess);
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}