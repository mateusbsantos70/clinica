# Clinic Essência (back-end + front juntos)

O front fica em `src/main/resources/static` e é servido pelo próprio Spring.
Abra http://localhost:8080/login.html (não use o Live Server).

## Como rodar
1. MySQL rodando, com o banco `essencia_viva` criado.
2. Configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no IntelliJ.
3. Defina as variáveis de ambiente do primeiro administrador (no IntelliJ: Run > Edit Configurations > Environment variables):
   `ADMIN_EMAIL=admin@luminosa.com;ADMIN_SENHA=UmaSenhaForte123`
4. Rode `ClinicEssenciaApplication`. O Flyway cria as tabelas e o administrador é criado na primeira vez.
5. Entre em http://localhost:8080/login.html.

## Rotas da API
- POST /api/auth/login   {email, senha}  -> cria a sessão (cookie HttpOnly)
- POST /api/auth/logout                  -> encerra a sessão
- GET  /api/auth/me                      -> dados de quem está logado
- POST /api/usuarios     (só ADMINISTRADOR) {nome, email, senha, perfil}
- GET  /api/health
## Variáveis de ambiente

No IntelliJ, abra:

`Run > Edit Configurations > Environment variables`

Configure:

```text
DB_URL=jdbc:mysql://127.0.0.1:3306/essencia_viva?serverTimezone=America/Sao_Paulo&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=clinic_app
DB_PASSWORD=senha_do_banco
ADMIN_EMAIL=admin@example.com
ADMIN_SENHA=uma_senha_com_no_minimo_8_caracteres
```

