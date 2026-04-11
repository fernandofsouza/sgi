package br.gov.sgi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.cloud.azure.active-directory.enabled:false}")
    private boolean entraIdEnabled;

    @Value("${sgi.jwt.issuer-uri:}")
    private String jwtIssuerUri;

    /**
     * Configuração de segurança.
     *
     * Quando ENTRA_ID_ENABLED=true (produção/homolog):
     *   - Valida tokens JWT emitidos pelo Azure Entra ID
     *   - Extrai roles/grupos via claims do token
     *
     * Quando ENTRA_ID_ENABLED=false (dev):
     *   - Endpoints públicos para facilitar desenvolvimento local
     *   - Apenas /actuator/** protegido
     */
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
                    .requestMatchers("/actuator/**").hasRole("SGI_ADMIN")
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> {
                        jwt.jwtAuthenticationConverter(jwtAuthConverter());
                        if (!jwtIssuerUri.isBlank()) {
                            jwt.decoder(JwtDecoders.fromIssuerLocation(jwtIssuerUri));
                        }
                    })
                );
        } else {
            // DEV: sem autenticação
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    /**
     * Converte claims do token Entra ID em roles Spring Security.
     * Mapeia claim "roles" para GrantedAuthorities com prefixo ROLE_.
     * Mapeia claim "groups" para GrantedAuthorities com prefixo SCOPE_.
     */
    @Bean
    public org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthConverter() {
        var grantedAuthoritiesConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        var jwtConverter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        // O subject (oid claim) do Entra ID é usado como principal name
        jwtConverter.setPrincipalClaimName("preferred_username");
        return jwtConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // Em prod, restrinja ao domínio Angular
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
