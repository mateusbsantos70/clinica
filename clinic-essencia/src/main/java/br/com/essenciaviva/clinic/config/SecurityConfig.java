package br.com.essenciaviva.clinic.config;

import br.com.essenciaviva.clinic.repository.SessaoRepository;
import br.com.essenciaviva.clinic.security.SessaoFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Argon2 (usa a biblioteca BouncyCastle que já está no pom.xml)
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http, SessaoRepository sessoes,
                                       @Value("${app.sessao.inatividade-minutos:30}") long inatividadeMinutos)
            throws Exception {

        SessaoFilter filtroSessao = new SessaoFilter(sessoes, inatividadeMinutos);

        http
                // O token fica em cookie HttpOnly + SameSite=Strict, que já impede
                // requisições vindas de outros sites. Por isso o CSRF do Spring fica desligado.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/api/health", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/**", "/swagger-ui/**", "/v3/api-docs/**").authenticated()
                        // páginas, CSS, JS e imagens do front
                        .anyRequest().permitAll())
                .addFilterBefore(filtroSessao, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
