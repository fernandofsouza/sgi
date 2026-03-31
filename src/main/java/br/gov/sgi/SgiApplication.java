package br.gov.sgi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SgiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SgiApplication.class, args);
    }
}
