package br.gov.sgi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.cloud.azure.active-directory.enabled:false}")
    private boolean entraIdEnabled;

    /**
     * CORS_ALLOWED_ORIGINS — variável de ambiente no Heroku.
     * Exemplos:
     *   https://sgi-frontend-abc123.herokuapp.com
     *   https://meudominio.com.br
     * Em dev usa "*" (permite qualquer origem).
     */
    @Value("${sgi.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (entraIdEnabled) {
            http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                );
        } else {
            // DEV / Heroku sem Entra ID: tudo público
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    @Bean
    public org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthConverter() {
        var converter = new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("ROLE_");

        var jwtConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        jwtConverter.setPrincipalClaimName("preferred_username");
        return jwtConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Suporta múltiplas origens separadas por vírgula
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        if (origins.contains("*")) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(origins);
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(!origins.contains("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
