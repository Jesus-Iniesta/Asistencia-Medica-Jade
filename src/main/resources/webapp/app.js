// ========== APLICACIÓN PRINCIPAL ==========
import { METODOS_PAGO } from './config.js';
import { state, resetState, setPacienteData } from './data.js';
import { generarIDUnico } from './utils.js';
import { showStep, resetFormularioCobro } from './ui.js';
import { procesarSignosVitales, procesarCita, procesarPago } from './steps.js';
import { generarRecetaMedica } from './receta.js';

// ========== INICIALIZACIÓN ==========
document.addEventListener('DOMContentLoaded', () => {
    inicializarEventos();
});

function inicializarEventos() {
    // Botón inicial
    document.getElementById('btnPasarRecepcion').addEventListener('click', () => {
        showStep('recepcion');
    });

    // Formulario de recepción
    document.getElementById('recepcionForm').addEventListener('submit', manejarRegistroPaciente);

    // Proceder al cobro
    document.getElementById('btnProcederCobro').addEventListener('click', () => {
        showStep('cobro');
        document.getElementById('cobro-nombre').textContent = state.currentPacienteData.nombre;
    });

    // Botones de pago
    document.querySelectorAll('.btn-pago').forEach(btn => {
        btn.addEventListener('click', function() {
            const metodo = this.getAttribute('data-metodo');
            procesarPago(metodo, METODOS_PAGO);
        });
    });

    // Ver receta médica
    document.getElementById('btnVerReceta').addEventListener('click', () => {
        generarRecetaMedica();
        showStep('receta');
    });

    // Imprimir receta
    document.getElementById('btnImprimirReceta').addEventListener('click', () => {
        window.print();
    });

    // Nueva consulta
    document.getElementById('btnNuevaConsulta').addEventListener('click', reiniciarAplicacion);
    document.getElementById('btnVolverInicio').addEventListener('click', reiniciarAplicacion);
}

// ========== MANEJADORES DE EVENTOS ==========
async function manejarRegistroPaciente(e) {
    e.preventDefault();

    const pacienteData = {
        nombre: document.getElementById('nombre').value,
        edad: document.getElementById('edad').value,
        genero: document.getElementById('genero').value,
        sintomas: document.getElementById('sintomas').value,
        pacienteId: generarIDUnico()
    };

    setPacienteData(pacienteData);

    try {
        showStep('enfermero');
        await procesarSignosVitales();
        await procesarCita();
    } catch (error) {
        console.error('❌ Error en el proceso:', error);

        // Mostrar mensaje de error al usuario
        alert(`⚠️ Error en el proceso:\n\n${error.message}\n\nAsegúrate de que:\n1. El servidor está ejecutándose\n2. Estás conectado a la misma red\n3. El firewall permite la conexión`);

        // Volver al inicio
        showStep('inicio');
    }
}

function reiniciarAplicacion() {
    resetState();
    document.getElementById('recepcionForm').reset();
    resetFormularioCobro();
    showStep('inicio');
}
