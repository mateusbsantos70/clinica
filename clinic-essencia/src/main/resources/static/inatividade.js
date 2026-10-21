// Depois de um tempo sem atividade, encerra a sessão no servidor e volta para o login.
const TEMPO_INATIVO = 15 * 60 * 1000; // 15 minutos (para testar, use 5000 = 5 segundos)
let temporizador;

async function sairPorInatividade() {
    try {
        await fetch("/api/auth/logout", { method: "POST", credentials: "same-origin" });
    } finally {
        window.location.replace("login.html");
    }
}

function reiniciarTempo() {
    clearTimeout(temporizador);
    temporizador = setTimeout(sairPorInatividade, TEMPO_INATIVO);
}

["mousemove", "mousedown", "keydown", "touchstart", "scroll"].forEach(function (evento) {
    document.addEventListener(evento, reiniciarTempo);
});

reiniciarTempo();
