package br.com.essenciaviva.clinic.controller;

import br.com.essenciaviva.clinic.model.Perfil;
import br.com.essenciaviva.clinic.repository.UsuarioRepository.Usuario;
import br.com.essenciaviva.clinic.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    public record NovoUsuario(
            @NotBlank(message = "Informe o nome.")
            @Size(max = 120, message = "Nome muito longo.")
            String nome,

            @NotBlank(message = "Informe o e-mail.")
            @Email(message = "E-mail inválido.")
            @Size(max = 180, message = "E-mail muito longo.")
            String email,

            @NotBlank(message = "Informe a senha.")
            @Size(min = 8, max = 128, message = "A senha precisa ter entre 8 e 128 caracteres.")
            String senha,

            @NotNull(message = "Escolha o perfil.")
            Perfil perfil) {
    }

    public record UsuarioCriado(String id, String nome, String email, String perfil) {
    }

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    /** Só ADMINISTRADOR chega aqui (regra no SecurityConfig). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioCriado criar(@Valid @RequestBody NovoUsuario dados) {
        Usuario u = service.criar(dados.nome(), dados.email(), dados.senha(), dados.perfil());
        return new UsuarioCriado(u.id(), u.nome(), u.email(), u.perfil());
    }
}
