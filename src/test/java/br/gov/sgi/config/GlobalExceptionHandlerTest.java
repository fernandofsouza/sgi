package br.gov.sgi.config;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound retorna HTTP 404 com mensagem de erro")
    void handleNotFoundDeveRetornar404() {
        EntityNotFoundException ex = new EntityNotFoundException("Indicador não encontrado: ind-99");

        ResponseEntity<?> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        // Verifica campos do ErrorResponse via toString/reflection
        Object body = response.getBody();
        assertThat(body.toString()).contains("404");
        assertThat(body.toString()).contains("Not Found");
        assertThat(body.toString()).contains("ind-99");
    }

    @Test
    @DisplayName("handleValidation retorna HTTP 400 com mapa de erros por campo")
    void handleValidationDeveRetornar400ComMapaDeErros() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("indicator", "title", "não deve estar em branco");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<?> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().toString()).contains("400");
        assertThat(response.getBody().toString()).contains("Validation Error");
    }

    @Test
    @DisplayName("handleGeneric retorna HTTP 500 com mensagem genérica")
    void handleGenericDeveRetornar500() {
        RuntimeException ex = new RuntimeException("Erro inesperado qualquer");

        ResponseEntity<?> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().toString()).contains("500");
        assertThat(response.getBody().toString()).contains("Internal Server Error");
        // Não deve vazar a mensagem original (segurança)
        assertThat(response.getBody().toString()).doesNotContain("Erro inesperado qualquer");
    }
}
