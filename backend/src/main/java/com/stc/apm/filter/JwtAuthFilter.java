package com.stc.apm.filter;

import com.stc.apm.constants.MainConstants;
import com.stc.apm.services.ApmUserService;
import com.stc.apm.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This class helps us to validate the generated jwt token
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApmUserService apmUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(MainConstants.COOKIE_HEADER_AUTHORIZATION);

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(MainConstants.COOKIE_HEADER_AUTHORIZATION)) {
                    authHeader = "Bearer " + cookie.getValue();
                    break;
                }
            }
        }

        String token = null;
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = apmUserService.loadUserByUsername(username);
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                response.addHeader(MainConstants.COOKIE_HEADER_LOGIN_STATUS, MainConstants.COOKIE_HEADER_LOGIN_STATUS_MESSAGE_SUCCESS);
            } else if (userDetails == null){
                response.addHeader(MainConstants.COOKIE_HEADER_LOGIN_STATUS, MainConstants.COOKIE_HEADER_LOGIN_STATUS_MESSAGE_FAILED);
            }
        }
        filterChain.doFilter(request, response);
    }
}
