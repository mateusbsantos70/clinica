package br.com.essenciaviva.clinic.controller;

import br.com.essenciaviva.clinic.repository.UsuarioRepository.Usuario;
import br.com.essenciaviva.clinic.security.SessaoFilter;
import br.com.essenciaviva.clinic.security.UsuarioLogado;
import br.com.essenciaviva.clinic.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail.")
            @Email(message = "E-mail inválido.")
            @Size(max = 180, message = "E-mail muito longo.")
            String email,

            @NotBlank(message = "Informe a senha.")
            @Size(max = 128, message = "Senha muito longa.")
            String senha) {
    }

    public record UsuarioResposta(String nome, String email, String perfil) {
    }

    private final AuthService auth;
    private final long duracaoHoras;
    private final boolean cookieSeguro;

    public AuthController(AuthService auth,
                          @Value("${app.sessao.duracao-horas:8}") long duracaoHoras,
                          @Value("${app.cookie-seguro:false}") boolean cookieSeguro) {
        this.auth = auth;
        this.duracaoHoras = duracaoHoras;
        this.cookieSeguro = cookieSeguro;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResposta> login(@Valid @RequestBody LoginRequest req) {
        AuthService.Login resultado = auth.autenticar(req.email(), req.senha());
        Usuario u = resultado.usuario();

        ResponseCookie cookie = ResponseCookie.from(SessaoFilter.COOKIE, resultado.token())
                .httpOnly(true)
                .secure(cookieSeguro)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofHours(duracaoHoras))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new UsuarioResposta(u.nome(), u.email(), u.perfil()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = SessaoFilter.lerToken(request);
        if (token != null) {
            auth.encerrar(token);
        }

        ResponseCookie apagar = ResponseCookie.from(SessaoFilter.COOKIE, "")
                .httpOnly(true)
                .secure(cookieSeguro)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, apagar.toString())
                .build();
    }

    @GetMapping("/me")
    public UsuarioResposta me(@AuthenticationPrincipal UsuarioLogado usuario) {
        return new UsuarioResposta(usuario.nome(), usuario.email(), usuario.perfil());
    }
}
