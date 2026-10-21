package br.com.essenciaviva.clinic.service;

import br.com.essenciaviva.clinic.model.Perfil;
import br.com.essenciaviva.clinic.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro administrador quando a tabela de usuários está vazia.
 * E-mail e senha vêm das variáveis de ambiente ADMIN_EMAIL e ADMIN_SENHA
 * (nada de senha fixa no código).
 */
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final UsuarioService servico;
    private final String email;
    private final String senha;

    public AdminInicial(UsuarioRepository usuarios, UsuarioService servico,
                        @Value("${app.admin.email:}") String email,
                        @Value("${app.admin.senha:}") String senha) {
        this.usuarios = usuarios;
        this.servico = servico;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (usuarios.existeAlgum()) {
            return;
        }

        if (email.isBlank() || senha.length() < 8) {
            log.warn("Nenhum usuário cadastrado. Defina ADMIN_EMAIL e ADMIN_SENHA "
                    + "(mínimo 8 caracteres) e reinicie para criar o primeiro administrador.");
            return;
        }

        servico.criar("Administrador", email, senha, Perfil.ADMINISTRADOR);
        log.info("Primeiro administrador criado: {}", email);
    }
}
