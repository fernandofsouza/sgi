package br.gov.sgi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Habilita segurança a nível de método (@PreAuthorize) somente quando
 * o Entra ID (Azure AD) está ativo. Em dev/labs (ENTRA_ID_ENABLED=false),
 * as anotações @PreAuthorize são ignoradas e todos os endpoints ficam acessíveis.
 */
@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(
        name = "spring.cloud.azure.active-directory.enabled",
        havingValue = "true"
)
public class MethodSecurityConfig {
}
