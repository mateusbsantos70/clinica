package br.com.essenciaviva.clinic.security;

import br.com.essenciaviva.clinic.repository.SessaoRepository;
import br.com.essenciaviva.clinic.util.Tempo;
import br.com.essenciaviva.clinic.util.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lê o cookie "sessao", confere no banco (pelo SHA-256 do token) e, se a sessão
 * estiver válida, marca a requisição como autenticada.
 * Não é um @Component de propósito: é criado no SecurityConfig para rodar só uma vez.
 */
public class SessaoFilter extends OncePerRequestFilter {

    public static final String COOKIE = "sessao";

    private final SessaoRepository sessoes;
    private final long inatividadeMinutos;

    public SessaoFilter(SessaoRepository sessoes, long inatividadeMinutos) {
        this.sessoes = sessoes;
        this.inatividadeMinutos = inatividadeMinutos;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Só as rotas da API precisam de sessão (páginas e imagens não)
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = lerToken(request);

        if (token != null) {
            LocalDateTime agora = Tempo.agora();
            sessoes.buscarAtiva(TokenUtil.sha256(token), agora, agora.minusMinutes(inatividadeMinutos))
                    .ifPresent(s -> {
                        sessoes.atualizarAtividade(s.sessaoId(), agora);
                        UsuarioLogado principal =
                                new UsuarioLogado(s.usuarioId(), s.nome(), s.email(), s.perfil());
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + s.perfil())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }

        chain.doFilter(request, response);
    }

    public static String lerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
