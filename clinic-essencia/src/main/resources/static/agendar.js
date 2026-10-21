const HORARIOS = ["08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"];

const form = document.getElementById("form-agendar");
const campoData = document.getElementById("data");
const caixaHorarios = document.getElementById("horarios");
const mensagem = document.getElementById("mensagem");

// Não deixa escolher datas passadas
campoData.min = new Date().toISOString().split("T")[0];

function lerAgendamentos() {
    try {
        return JSON.parse(localStorage.getItem("agendamentos")) || [];
    } catch (erro) {
        return [];
    }
}

function salvarAgendamentos(lista) {
    try {
        localStorage.setItem("agendamentos", JSON.stringify(lista));
    } catch (erro) {
        // se o navegador bloquear o armazenamento, o agendamento não é salvo
    }
}

function mostrarMensagem(texto, tipo) {
    mensagem.textContent = texto;
    mensagem.className = tipo;
}

function mostrarHorarios() {
    const data = campoData.value;
    caixaHorarios.innerHTML = "";

    if (!data) {
        caixaHorarios.innerHTML = '<p class="aviso">Escolha uma data para ver os horários.</p>';
        return;
    }

    const ocupados = lerAgendamentos()
        .filter(function (a) { return a.data === data; })
        .map(function (a) { return a.hora; });

    HORARIOS.forEach(function (hora) {
        const rotulo = document.createElement("label");
        rotulo.className = "horario";
        rotulo.innerHTML =
            '<input type="radio" name="hora" value="' + hora + '"' +
            (ocupados.includes(hora) ? " disabled" : "") + '>' +
            "<span>" + hora + "</span>";
        caixaHorarios.appendChild(rotulo);
    });
}

campoData.addEventListener("change", function () {
    mensagem.textContent = "";
    mostrarHorarios();
});

form.addEventListener("submit", function (evento) {
    evento.preventDefault();

    const nome = form.nome.value.trim();
    const telefone = form.telefone.value.trim();
    const tratamento = form.tratamento.value;
    const data = form.data.value;
    const horaEscolhida = form.querySelector('input[name="hora"]:checked');

    if (!nome || !telefone || !tratamento || !data) {
        mostrarMensagem("Preencha todos os campos.", "erro");
        return;
    }

    if (!horaEscolhida) {
        mostrarMensagem("Escolha um horário.", "erro");
        return;
    }

    const lista = lerAgendamentos();
    const hora = horaEscolhida.value;

    // Confere de novo se o horário ainda está livre
    if (lista.some(function (a) { return a.data === data && a.hora === hora; })) {
        mostrarMensagem("Esse horário acabou de ser ocupado. Escolha outro.", "erro");
        mostrarHorarios();
        return;
    }

    lista.push({ nome: nome, telefone: telefone, tratamento: tratamento, data: data, hora: hora });
    salvarAgendamentos(lista);

    const dataBR = data.split("-").reverse().join("/");
    mostrarMensagem("Consulta agendada para " + dataBR + " às " + hora + ".", "ok");
    mostrarHorarios();
});
