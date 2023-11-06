package com.jacob.apm.config;

/**
 * This class is generated by referring:
 * https://www.geeksforgeeks.org/spring-boot-3-0-jwt-authentication-with-spring-security-using-mysql-database/#
 */

import com.jacob.apm.filter.JwtAuthFilter;
import com.jacob.apm.services.APMUserService;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // User Creation
    @Bean
    public UserDetailsService userDetailsService() {
        return new APMUserService();
    }

    // Configuring HttpSecurity
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                "/status",
                                "/login",
                                "/signup",
                                "/",
                                "/auth/logout",
                                "/apiCall/save",
                                "/auth/addNewUser",
                                "/auth/isUsernameAvailable",
                                "/error",
                                "/js/*",
                                "/css/*",
                                "/images/*"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/generateToken").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/processLogin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/apiCall/getAll").authenticated()
                        .requestMatchers(HttpMethod.POST, "/apiCall/getAll/range").authenticated()
                        .requestMatchers(HttpMethod.GET, "/home").authenticated()
                        .requestMatchers(HttpMethod.POST, "/apiCall/saveFromApmDashBoard").authenticated()
                )
                .formLogin((formLogin) ->
                        formLogin
                                .loginPage("/login")
                                .defaultSuccessUrl("/status")
                                .permitAll() // Allow unauthenticated users to access the login page
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    // Password Encoding
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}



