package com.fmuller.authenticate.app.auth.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmuller.authenticate.app.auth.service.IJWTService;
import com.fmuller.authenticate.app.auth.service.JWTServiceImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    private IJWTService jwtService;


    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, IJWTService jwtService) {
        super(authenticationManager);
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader(JWTServiceImpl.HEADER_STRING);

        if (header == null || !header.startsWith(JWTServiceImpl.TOKEN_PREFIX)) {
            logger.error("no existe autorization");
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = null;
        if (jwtService.validate(header)) {
            logger.info(jwtService.getRoles(header));
            authentication = new UsernamePasswordAuthenticationToken(jwtService.getUsername(header), null, jwtService.getRoles(header));
            
        }else{
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("error", "no tienes autorizacion");

            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(200);
            response.setContentType("application/json");
            return;
        }
                
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);

    }
    

}
