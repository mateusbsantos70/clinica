package br.com.essenciaviva.clinic.service;

import br.com.essenciaviva.clinic.repository.SessaoRepository;
import br.com.essenciaviva.clinic.repository.UsuarioRepository;
import br.com.essenciaviva.clinic.repository.UsuarioRepository.Usuario;
import br.com.essenciaviva.clinic.util.Tempo;
import br.com.essenciaviva.clinic.util.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final int MAX_FALHAS = 5;
    private static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(15);

    public record Login(String token, Usuario usuario) {
    }

    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;
    private final PasswordEncoder encoder;
    private final long duracaoHoras;
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, SessaoRepository sessoes, PasswordEncoder encoder,
                       @Value("${app.sessao.duracao-horas:8}") long duracaoHoras) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.encoder = encoder;
        this.duracaoHoras = duracaoHoras;
        // Usado quando o e-mail não existe, para o tempo de resposta ser parecido
        this.hashFalso = encoder.encode("senha-falsa-para-igualar-o-tempo");
    }

    public Login autenticar(String email, String senha) {
        LocalDateTime agora = Tempo.agora();
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarios.buscarPorEmail(emailNormalizado).orElse(null);

        if (usuario == null) {
            encoder.matches(senha, hashFalso);
            throw credenciaisInvalidas();
        }

        if (usuario.bloqueadoAte() != null && usuario.bloqueadoAte().isAfter(agora)) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Muitas tentativas. Conta bloqueada por alguns minutos.");
        }

        if (!usuario.ativo() || !encoder.matches(senha, usuario.senhaHash())) {
            if (usuario.ativo()) {
                registrarFalha(usuario, agora);
            }
            throw credenciaisInvalidas();
        }

        if (usuario.falhasLogin() > 0 || usuario.bloqueadoAte() != null) {
            usuarios.zerarFalhas(usuario.id());
        }

        String token = TokenUtil.gerar();
        sessoes.inserir(UUID.randomUUID().toString(), usuario.id(), TokenUtil.sha256(token),
                agora, agora.plusHours(duracaoHoras));

        return new Login(token, usuario);
    }

    public void encerrar(String token) {
        sessoes.invalidar(TokenUtil.sha256(token), Tempo.agora());
    }

    private void registrarFalha(Usuario usuario, LocalDateTime agora) {
        int falhas = usuario.falhasLogin() + 1;
        if (falhas >= MAX_FALHAS) {
            usuarios.registrarFalha(usuario.id(), 0, agora.plus(TEMPO_BLOQUEIO));
        } else {
            usuarios.registrarFalha(usuario.id(), falhas, null);
        }
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos.");
    }
}
