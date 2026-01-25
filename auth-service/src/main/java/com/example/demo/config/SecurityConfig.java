package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configure HTTP security with CORS and stateless sessions
        http.cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource))
            .csrf(csrfConfig -> csrfConfig.disable())
            .authorizeHttpRequests(requestAuth -> 
                configureRequestAuthorization(requestAuth)
            )
            .sessionManagement(sessionConfig -> 
                sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    private void configureRequestAuthorization(org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        // Public endpoints - no authentication required
        auth.requestMatchers("/auth/**").permitAll();
        // All other endpoints accessible for development
        auth.anyRequest().permitAll();
    }

    @Bean
    public PasswordEncoder createPasswordEncoder() {
        // BCrypt with strength 10 for password hashing
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager createAuthManager(
            UserDetailsService detailsService, 
            PasswordEncoder encoder) {
        // Setup DAO authentication provider
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(detailsService);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}