package com.example.sdvnews.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        extractToken(request)
                .flatMap(jwtUtil::validate)
                .ifPresent(claims -> setAuthentication(claims));

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        // API Gateway は Authorization を Cloud Run 認証用に書き換えるため、
        // 元のクライアント JWT は X-Forwarded-Authorization に転送される
        for (String headerName : new String[]{"X-Forwarded-Authorization", "Authorization"}) {
            String header = request.getHeader(headerName);
            if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
                return Optional.of(header.substring(BEARER_PREFIX.length()));
            }
        }
        return Optional.empty();
    }

    private void setAuthentication(Claims claims) {
        String userId = jwtUtil.extractUserId(claims);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
