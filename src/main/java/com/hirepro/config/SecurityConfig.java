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
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ✅ Always permit OPTIONS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // =====================
                        // PUBLIC ENDPOINTS
                        // Note: No /api prefix — context-path /api is already applied by server
                        // =====================
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/refresh-token",
                                "/auth/logout",
                                "/public/**",
                                "/error"
                        ).permitAll()

                        // =====================
                        // SWAGGER / API DOCS
                        // =====================
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // =====================
                        // ACTUATOR
                        // =====================
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("SUPERADMIN")

                        // =====================
                        // CLIENT MANAGEMENT
                        // ✅ Removed /api prefix — server context-path is already /api
                        // =====================
                        .requestMatchers("/clients/**").hasRole("SUPERADMIN")

                        // =====================
                        // USER MANAGEMENT
                        // ✅ Removed /api prefix
                        // =====================
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("SUPERADMIN")
                        .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/client/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/{id}").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")

                        // =====================
                        // ROLE & PERMISSION
                        // ✅ Removed /api prefix
                        // =====================
                        .requestMatchers("/roles/**").hasRole("SUPERADMIN")
                        .requestMatchers("/permissions/**").hasRole("SUPERADMIN")

                        // =====================
                        // REPORTS
                        // ✅ Removed /api prefix
                        // =====================
                        .requestMatchers("/reports/**").hasAnyRole("SUPERADMIN", "CLIENT_ADMIN")

                        // =====================
                        // DEBUG (remove after testing)
                        // =====================
                        .requestMatchers("/debug/**").authenticated()

                        // Any other request must be authenticated
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
    // CORS (integrated with Spring Security filter chain)
    // =========================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://consulting-hirepro-ui-app-development.up.railway.app"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}