package com.frsystem.configuration;

import com.frsystem.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints
                        .requestMatchers("/api/v1/auth/register/passenger").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/register/admin").hasAuthority("ADMIN")

                        // PASSENGER READ ONLY
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/airplanes/**", "/api/v1/airports/**").authenticated()

                        //ADMIN CAN REACH OTHER METHODS (PUT, DELETE, POST)
                        .requestMatchers("/api/v1/airplanes/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/airports/**").hasAuthority("ADMIN")

                        // Flight Management
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/flights/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/flights/search").permitAll()
                        .requestMatchers("/api/v1/flights/**").hasAuthority("ADMIN")

                        // Reservation Management
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/reservations/*/admin-cancel").hasAuthority("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/reservations").hasAuthority("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/reservations/me").authenticated()
                        .requestMatchers("/api/v1/reservations/**").authenticated()

                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
