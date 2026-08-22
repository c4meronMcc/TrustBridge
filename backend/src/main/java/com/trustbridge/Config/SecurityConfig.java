package com.trustbridge.Config;

import com.trustbridge.Security.JwtAuthenticationFilter; // Adjust this import if your filter is in a different package
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Configures web security to ignore specific request matchers.
     *
     * @return a {@code WebSecurityCustomizer} instance that specifies the paths to be ignored by the security filter chain.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/v1/webhooks/**");
    }

    /**
     * Configures the security filter chain for the application, including CORS, CSRF, stateless session management,
     * request authorization, and JWT authentication filtering.
     *
     * @param http the {@link HttpSecurity} object used to configure web-based security in the application.
     * @return a {@link SecurityFilterChain} instance that defines the security configuration for the application.
     * @throws Exception if there is a failure during security configuration setup.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/v1/webhooks/Stripe").permitAll()
                        .requestMatchers("/api/v1/webhooks/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/invite/**").permitAll()
                        .requestMatchers("/api/files/**").authenticated()
                        .anyRequest().authenticated()
                ).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a CORS configuration source used to define behavior for handling
     * cross-origin resource sharing (CORS) in the application.
     *
     * Configures allowed origins, HTTP methods, headers, and credentials to ensure
     * secure communication between the client and server. The configuration is registered
     * for all endpoints in the application.
     *
     * @return a {@link CorsConfigurationSource} instance containing the configured CORS rules.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use your injected origins from application.properties
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Note: Browsers reject allowedHeaders("*") when allowCredentials is true,
        // so we explicitly list them to be safe.
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Cookie"));

        // This allows your Next.js secure cookies to pass through
        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Creates and provides an {@link AuthenticationManager} bean for the application.
     *
     * This method retrieves the authentication manager from the given {@link AuthenticationConfiguration}.
     * The {@link AuthenticationManager} is responsible for handling authentication requests
     * and integrating with configured authentication providers.
     *
     * @param config the {@link AuthenticationConfiguration} object that contains the security configuration
     *               for authentication management.
     * @return the {@link AuthenticationManager} instance configured for the application.
     * @throws Exception if an error occurs while accessing the authentication manager from the configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    /**
     * Provides a {@link PasswordEncoder} bean for encoding and verifying passwords.
     *
     * This method returns an instance of {@link BCryptPasswordEncoder}, which
     * uses the BCrypt hashing algorithm to securely hash passwords. BCrypt is
     * designed to be computationally intensive to protect against brute force attacks.
     *
     * @return a {@link PasswordEncoder} instance for handling password encryption.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}