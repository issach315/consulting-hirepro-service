package com.hirepro.auth.filter;

import com.hirepro.auth.util.CookieUtil;
import com.hirepro.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CookieUtil cookieUtil) {
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug(">>> JwtFilter: {} {}", request.getMethod(), request.getRequestURI());

        try {
            String token = null;

            // 1. Try cookie first
            var cookieToken = cookieUtil.getAccessTokenFromCookie(request);
            if (cookieToken.isPresent()) {
                token = cookieToken.get();
                log.debug("Token source: cookie");
            } else {
                // 2. Fallback: Authorization header
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    log.debug("Token source: Authorization header");
                }
            }

            if (token == null) {
                log.debug("No token found for URI: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtUtil.extractUsername(token);
            log.debug("Token username: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(token, username)) {
                    String role = jwtUtil.extractRole(token);
                    log.debug("Token role claim: {}", role);

                    // ✅ Guard against double ROLE_ prefix
                    // JWT stores "SUPERADMIN" → authority = "ROLE_SUPERADMIN" ✓
                    // JWT stores "ROLE_SUPERADMIN" → authority = "ROLE_SUPERADMIN" ✓ (not ROLE_ROLE_SUPERADMIN)
                    String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    log.debug("Setting authority: {}", authorityName);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority(authorityName))
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Auth set for user: {} with authorities: {}", username, authToken.getAuthorities());
                } else {
                    log.warn("Token validation failed for username: {}", username);
                }
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}