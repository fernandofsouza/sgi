package br.gov.sgi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SGI — Sistema de Gestão de Indicadores")
                        .description("API REST para gerenciamento de indicadores, check-ins, critérios de relevância e membros de equipe.")
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("SGI")
                                .email("sgi@gov.br")))
                .tags(List.of(
                        new Tag().name("Indicadores")
                                .description("Criação, consulta, atualização e remoção de indicadores; check-ins e avaliação de relevância"),
                        new Tag().name("Membros da Equipe")
                                .description("Gestão dos membros da equipe responsáveis pelos indicadores"),
                        new Tag().name("Critérios de Relevância")
                                .description("Critérios e escala de relevância utilizados na avaliação dos indicadores"),
                        new Tag().name("Configurações do Sistema")
                                .description("Leitura e atualização dos status configuráveis do sistema"),
                        new Tag().name("Audit Log")
                                .description("Registro e consulta do log de auditoria das operações")
                ));
    }
}
