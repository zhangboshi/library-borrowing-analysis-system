package com.example.library.interceptor;

import com.example.library.common.ApiResponse;
import com.example.library.common.CurrentUser;
import com.example.library.common.CurrentUserHolder;
import com.example.library.common.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow unauthenticated GET for listing endpoints
        if ("GET".equalsIgnoreCase(method) && antPathMatcher.match("/api/borrow-records/**", path)) {
            return true;
        }

        // Allow login and health endpoints
        if (antPathMatcher.match("/api/auth/login", path) || antPathMatcher.match("/api/health", path)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Missing token");
            return false;
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parse(token);
            CurrentUser user = new CurrentUser(claims.get("username", String.class), claims.get("role", String.class));
            CurrentUserHolder.set(user);
            return true;
        } catch (Exception ex) {
            writeUnauthorized(response, "Invalid or expired token");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        String body = "{\"success\":false,\"message\":\"" + message + "\"}";
        response.getWriter().write(body);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserHolder.clear();
    }
}
