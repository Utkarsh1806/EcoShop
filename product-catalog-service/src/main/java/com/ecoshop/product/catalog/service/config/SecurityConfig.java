package com.ecoshop.product.catalog.service.config;

import com.ecoshop.common.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Public catalog reads
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**", "/api/brands/**").permitAll()
                // Writes require admin/seller (role check via JWT claim)
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.POST, "/api/categories/**", "/api/brands/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/brands/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
