const formLogin = document.getElementById("form-login");
const formCadastro = document.getElementById("form-cadastro");
const mensagem = document.getElementById("mensagem-erro");

function mostrarMensagem(texto, tipo) {
    mensagem.textContent = texto;
    mensagem.className = tipo || "";
}

async function enviarJson(url, dados) {
    const resposta = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin",
        body: JSON.stringify(dados)
    });
    const corpo = await resposta.json().catch(function () { return {}; });
    return { ok: resposta.ok, status: resposta.status, corpo: corpo };
}

// ---------- LOGIN ----------
if (formLogin) {
    formLogin.addEventListener("submit", async function (evento) {
        evento.preventDefault();
        mostrarMensagem("");

        const botao = formLogin.querySelector("button[type='submit']");
        botao.disabled = true;

        try {
            const r = await enviarJson("/api/auth/login", {
                email: formLogin.email.value.trim(),
                senha: formLogin.senha.value
            });

            if (r.ok) {
                window.location.href = "inicio.html";
                return;
            }
            mostrarMensagem(r.corpo.mensagem || "Não foi possível entrar.");
        } catch (erro) {
            mostrarMensagem("Não foi possível conectar ao servidor.");
        } finally {
            botao.disabled = false;
        }
    });
}

// ---------- NOVO USUÁRIO (só administrador) ----------
if (formCadastro) {
    formCadastro.addEventListener("submit", async function (evento) {
        evento.preventDefault();
        mostrarMensagem("");

        if (formCadastro.senha.value !== formCadastro.confirmar.value) {
            mostrarMensagem("As senhas não são iguais.");
            return;
        }

        const botao = formCadastro.querySelector("button[type='submit']");
        botao.disabled = true;

        try {
            const r = await enviarJson("/api/usuarios", {
                nome: formCadastro.nome.value.trim(),
                email: formCadastro.email.value.trim(),
                senha: formCadastro.senha.value,
                perfil: formCadastro.perfil.value
            });

            if (r.status === 401) {
                window.location.replace("login.html");
                return;
            }

            if (r.ok) {
                formCadastro.reset();
                mostrarMensagem("Usuário cadastrado com sucesso.", "ok");
            } else if (r.status === 403) {
                mostrarMensagem("Você não tem permissão para cadastrar usuários.");
            } else {
                mostrarMensagem(r.corpo.mensagem || "Não foi possível cadastrar.");
            }
        } catch (erro) {
            mostrarMensagem("Não foi possível conectar ao servidor.");
        } finally {
            botao.disabled = false;
        }
    });
}
