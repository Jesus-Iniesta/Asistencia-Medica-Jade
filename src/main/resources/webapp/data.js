// ========== ESTADO GLOBAL ==========
export const state = {
    currentCita: null,
    currentPacienteData: null,
    datosCompletosConsulta: {
        paciente: null,
        signosVitales: null,
        diagnostico: null,
        pago: null
    }
};

export function resetState() {
    state.currentCita = null;
    state.currentPacienteData = null;
    state.datosCompletosConsulta = {
        paciente: null,
        signosVitales: null,
        diagnostico: null,
        pago: null
    };
}

export function setPacienteData(data) {
    state.currentPacienteData = data;
    state.datosCompletosConsulta.paciente = data;
}

export function setSignosVitales(signos) {
    state.datosCompletosConsulta.signosVitales = signos;
}

export function setDiagnostico(diagnostico) {
    state.datosCompletosConsulta.diagnostico = diagnostico;
}

export function setPago(pago) {
    state.datosCompletosConsulta.pago = pago;
}

