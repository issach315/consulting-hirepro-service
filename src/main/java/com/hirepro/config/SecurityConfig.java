package com.hirepro.config;

import com.hirepro.auth.filter.JwtAuthenticationFilter;
import com.hirepro.auth.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/refresh-token",
                                "/auth/logout",
                                "/public/**",
                                "/error"
                        ).permitAll()
                        // Swagger/API docs
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Actuator endpoints
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("SUPERADMIN")
                        // Client management
                        .requestMatchers("/clients/**").hasRole("SUPERADMIN")
                        // User management
                        .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("SUPERADMIN")
                        .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/client/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/{id}").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        // Role management
                        .requestMatchers("/roles/**").hasRole("SUPERADMIN")
                        // Permission management
                        .requestMatchers("/permissions/**").hasRole("SUPERADMIN")
                        // Reports
                        .requestMatchers("/reports/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // CORS FILTER (Global)
    // =========================
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies / Authorization headers)
        config.setAllowCredentials(true);

        // Allowed frontend origins (local + deployed)
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://consulting-hirepro-ui-app-development.up.railway.app"
        ));

        // Allowed headers
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        // Exposed headers (optional, for JWT)
        config.setExposedHeaders(List.of("Authorization"));

        // Allowed methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Preflight cache duration
        config.setMaxAge(3600L);

        // Register config for all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
