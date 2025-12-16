package com.example.library.interceptor;

import com.example.library.common.ApiResponse;
import com.example.library.common.CurrentUser;
import com.example.library.common.CurrentUserHolder;
import com.example.library.common.JwtUtil;
import com.example.library.common.RequireRole;
import com.example.library.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final TokenService tokenService;

    public AuthInterceptor(JwtUtil jwtUtil, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
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
            if (!tokenService.isTokenValid(token)) {
                writeUnauthorized(response, "Token expired or revoked");
                return false;
            }
            if (handler instanceof HandlerMethod hm) {
                RequireRole roleAnno = hm.getMethodAnnotation(RequireRole.class);
                if (roleAnno == null) {
                    roleAnno = hm.getBeanType().getAnnotation(RequireRole.class);
                }
                if (roleAnno != null) {
                    boolean allowed = List.of(roleAnno.value()).stream()
                            .filter(Objects::nonNull)
                            .anyMatch(r -> r.equalsIgnoreCase(user.getRole()));
                    if (!allowed) {
                        writeUnauthorized(response, "Forbidden");
                        return false;
                    }
                }
            }
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
