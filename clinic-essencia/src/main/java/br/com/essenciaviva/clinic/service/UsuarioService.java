package br.com.essenciaviva.clinic.service;

import br.com.essenciaviva.clinic.model.Perfil;
import br.com.essenciaviva.clinic.repository.UsuarioRepository;
import br.com.essenciaviva.clinic.repository.UsuarioRepository.Usuario;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public Usuario criar(String nome, String email, String senha, Perfil perfil) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        if (usuarios.buscarPorEmail(emailNormalizado).isPresent()) {
            throw emailJaCadastrado();
        }

        Usuario novo = new Usuario(
                UUID.randomUUID().toString(),
                nome.trim(),
                emailNormalizado,
                encoder.encode(senha),
                perfil.name(),
                0,
                null,
                true);

        try {
            usuarios.inserir(novo);
        } catch (DuplicateKeyException e) {
            throw emailJaCadastrado();
        }

        return novo;
    }

    private ResponseStatusException emailJaCadastrado() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail.");
    }
}
