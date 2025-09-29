package com.example.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HtmlLoginRedirectEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();

        // If the request is for an HTML page (browser) or admin path, redirect to login
        // page
        if ((accept != null && accept.contains("text/html")) || uri.endsWith(".html") || uri.startsWith("/admin")) {
            response.sendRedirect("/login.html");
            return;
        }

        // For API / non-browser callers, return 401 JSON style
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
