package com.ikn.ums.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ikn.ums.tenant.core.filter.JwtAuthorizationFilter;
import com.ikn.ums.tenant.core.filter.TenantContextFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class DocumentSecurityConfig {

    private final Environment environment;
    private final TenantContextFilter tenantContextFilter;

    public DocumentSecurityConfig(Environment environment,
                                  TenantContextFilter tenantContextFilter) {
        this.environment = environment;
        this.tenantContextFilter = tenantContextFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthorizationFilter jwtFilter =
                new JwtAuthorizationFilter(environment);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(tenantContextFilter,
                    UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

