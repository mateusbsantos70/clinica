package br.com.essenciaviva.clinic.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;

/** Devolve todos os erros da API no formato {"mensagem": "..."} para o front exibir. */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        String mensagem = e.getReason() != null ? e.getReason() : "Erro na requisição.";
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("mensagem", mensagem));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> Objects.requireNonNullElse(f.getDefaultMessage(), "Dados inválidos."))
                .orElse("Dados inválidos.");
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("mensagem", "Requisição inválida."));
    }
}
