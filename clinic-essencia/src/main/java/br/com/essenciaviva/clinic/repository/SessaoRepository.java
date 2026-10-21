package br.com.essenciaviva.clinic.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SessaoRepository {

    public record SessaoAtiva(String sessaoId, String usuarioId, String nome, String email, String perfil) {
    }

    private final JdbcTemplate jdbc;

    public SessaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void inserir(String id, String usuarioId, String tokenHash,
                        LocalDateTime agora, LocalDateTime expiraEm) {
        jdbc.update(
                "INSERT INTO sessao (id, usuario_id, token_hash, criada_em, ultima_atividade_em, expira_em) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                id, usuarioId, tokenHash, agora, agora, expiraEm);
    }

    /** Sessão válida = não invalidada, não expirada, com atividade recente e usuário ativo. */
    public Optional<SessaoAtiva> buscarAtiva(String tokenHash, LocalDateTime agora,
                                             LocalDateTime limiteInatividade) {
        List<SessaoAtiva> lista = jdbc.query(
                "SELECT s.id AS sessao_id, u.id AS usuario_id, u.nome, u.email, u.perfil "
                        + "FROM sessao s JOIN usuario u ON u.id = s.usuario_id "
                        + "WHERE s.token_hash = ? AND s.invalidada_em IS NULL "
                        + "AND s.expira_em > ? AND s.ultima_atividade_em > ? AND u.ativo = TRUE",
                (rs, i) -> new SessaoAtiva(
                        rs.getString("sessao_id"),
                        rs.getString("usuario_id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("perfil")),
                tokenHash, agora, limiteInatividade);
        return lista.stream().findFirst();
    }

    public void atualizarAtividade(String sessaoId, LocalDateTime agora) {
        jdbc.update("UPDATE sessao SET ultima_atividade_em = ? WHERE id = ?", agora, sessaoId);
    }

    public void invalidar(String tokenHash, LocalDateTime agora) {
        jdbc.update(
                "UPDATE sessao SET invalidada_em = ? WHERE token_hash = ? AND invalidada_em IS NULL",
                agora, tokenHash);
    }
}
