package br.com.essenciaviva.clinic.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    public record Usuario(String id, String nome, String email, String senhaHash, String perfil,
                          int falhasLogin, LocalDateTime bloqueadoAte, boolean ativo) {
    }

    private static final String COLUNAS =
            "id, nome, email, senha_hash, perfil, falhas_login, bloqueado_ate, ativo";

    private static final RowMapper<Usuario> MAPPER = (rs, i) -> new Usuario(
            rs.getString("id"),
            rs.getString("nome"),
            rs.getString("email"),
            rs.getString("senha_hash"),
            rs.getString("perfil"),
            rs.getInt("falhas_login"),
            rs.getObject("bloqueado_ate", LocalDateTime.class),
            rs.getBoolean("ativo"));

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        List<Usuario> lista = jdbc.query(
                "SELECT " + COLUNAS + " FROM usuario WHERE email = ?", MAPPER, email);
        return lista.stream().findFirst();
    }

    public boolean existeAlgum() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class);
        return total != null && total > 0;
    }

    public void inserir(Usuario u) {
        jdbc.update(
                "INSERT INTO usuario (id, nome, email, senha_hash, perfil) VALUES (?, ?, ?, ?, ?)",
                u.id(), u.nome(), u.email(), u.senhaHash(), u.perfil());
    }

    public void registrarFalha(String id, int falhas, LocalDateTime bloqueadoAte) {
        jdbc.update(
                "UPDATE usuario SET falhas_login = ?, bloqueado_ate = ? WHERE id = ?",
                falhas, bloqueadoAte, id);
    }

    public void zerarFalhas(String id) {
        jdbc.update(
                "UPDATE usuario SET falhas_login = 0, bloqueado_ate = NULL WHERE id = ?", id);
    }
}
