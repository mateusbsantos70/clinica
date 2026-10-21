// Use em toda página que só pode ser vista com login.
// A página começa escondida (<body hidden>) e só aparece depois que o servidor confirma a sessão.
(async function () {
    let usuario;

    try {
        const resposta = await fetch("/api/auth/me", { credentials: "same-origin" });
        if (!resposta.ok) {
            throw new Error("sem sessão");
        }
        usuario = await resposta.json();
    } catch (erro) {
        window.location.replace("login.html");
        return;
    }

    // Página exclusiva de um perfil (ex.: data-perfil-exigido="ADMINISTRADOR")
    const exigido = document.body.dataset.perfilExigido;
    if (exigido && usuario.perfil !== exigido) {
        window.location.replace("inicio.html");
        return;
    }

    const saudacao = document.getElementById("saudacao");
    if (saudacao) {
        saudacao.textContent = "Olá, " + usuario.nome + "!";
    }

    // Links só para administradores
    const linkCadastro = document.getElementById("link-cadastro");
    if (linkCadastro && usuario.perfil === "ADMINISTRADOR") {
        linkCadastro.hidden = false;
    }

    const botaoSair = document.getElementById("sair");
    if (botaoSair) {
        botaoSair.addEventListener("click", async function () {
            await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
            window.location.replace("login.html");
        });
    }

    document.body.hidden = false;
})();
