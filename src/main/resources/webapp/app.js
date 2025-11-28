const API_URL = 'http://localhost:7070/api';

let currentCita = null;
let eventSource = null;

// Generar ID único para cada paciente (timestamp + random)
function generarIDUnico() {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 1000);
    return `P${timestamp}${random}`;
}

// Manejo del formulario
document.getElementById('citaForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const cita = {
        nombre: document.getElementById('nombre').value,
        pacienteId: generarIDUnico(), // 🔥 ID único automático
        sintomas: document.getElementById('sintomas').value
    };

    currentCita = cita;

    // Actualizar estado
    updateStatus('enviando', 'Enviando solicitud al recepcionista...');

    try {
        const response = await fetch(`${API_URL}/cita`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(cita)
        });

        const data = await response.json();

        if (data.status === 'success') {
            updateStatus('recepcion', 'Recepcionista procesando solicitud...');

            // Simular progreso de agentes
            setTimeout(() => {
                updateStatus('enfermero', 'Enfermero tomando signos vitales...');
            }, 2000);

            setTimeout(() => {
                updateStatus('doctor', 'Doctor realizando diagnóstico...');
            }, 5000);

            // Iniciar polling para obtener el diagnóstico
            startPollingDiagnostico(cita.pacienteId);
        }
    } catch (error) {
        console.error('Error:', error);
        updateStatus('error', 'Error al procesar la solicitud');
    }
});

function updateStatus(step, message) {
    const estadoDiv = document.getElementById('estado');

    const icons = {
        enviando: '📤',
        recepcion: '📋',
        enfermero: '💉',
        doctor: '👨‍⚕️',
        completado: '✅',
        error: '❌'
    };

    const html = `
        <div class="status-step">
            <div class="icon">${icons[step]}</div>
            <div class="text">
                <h3>${getStepTitle(step)}</h3>
                <p>${message}</p>
            </div>
            ${step !== 'completado' && step !== 'error' ? '<div class="loading"></div>' : ''}
        </div>
    `;

    estadoDiv.innerHTML = html;
}

function getStepTitle(step) {
    const titles = {
        enviando: 'Enviando Solicitud',
        recepcion: 'En Recepción',
        enfermero: 'Con Enfermero',
        doctor: 'Consulta Médica',
        completado: 'Consulta Completada',
        error: 'Error en el Proceso'
    };
    return titles[step] || step;
}

async function startPollingDiagnostico(pacienteId) {
    let attempts = 0;
    const maxAttempts = 20;

    const interval = setInterval(async () => {
        attempts++;

        try {
            const response = await fetch(`${API_URL}/diagnostico/${pacienteId}`);
            const data = await response.json();

            console.log(`Intento ${attempts}:`, data);

            // Verificar que el diagnóstico esté completo
            if (data.pacienteId && data.diagnostico &&
                data.diagnostico !== "En proceso..." &&
                data.diagnostico !== null) {

                clearInterval(interval);
                updateStatus('completado', 'Consulta finalizada exitosamente');
                displayDiagnostico(data);

            } else if (attempts >= maxAttempts) {
                clearInterval(interval);
                updateStatus('error', 'Tiempo de espera agotado');
                console.error('Diagnóstico no recibido después de ' + maxAttempts + ' intentos');
            } else {
                console.log(`Esperando diagnóstico... (${attempts}/${maxAttempts})`);
            }
        } catch (error) {
            console.error('Error polling:', error);
            if (attempts >= maxAttempts) {
                clearInterval(interval);
                updateStatus('error', 'Error al obtener diagnóstico');
            }
        }
    }, 2000);
}

function displayDiagnostico(data) {
    const diagnosticoDiv = document.getElementById('diagnostico');

    const html = `
        <div class="alert-success">
            <h3>✅ Diagnóstico Completado</h3>
        </div>
        
        <div class="diagnostico-content">
            <div class="diagnostico-item">
                <h4>🩺 Diagnóstico</h4>
                <p>${data.diagnostico}</p>
            </div>
            
            <div class="diagnostico-item">
                <h4>💊 Tratamiento Recomendado</h4>
                <p>${data.tratamiento}</p>
            </div>
            
            <div class="diagnostico-item">
                <h4>📅 Próxima Cita</h4>
                <p>${data.fechaProxima}</p>
            </div>
        </div>
        
        <button onclick="resetForm()" class="btn-primary" style="margin-top: 24px;">
            Nueva Consulta
        </button>
    `;

    diagnosticoDiv.innerHTML = html;
}

function resetForm() {
    document.getElementById('citaForm').reset();
    document.getElementById('estado').innerHTML = '<p>Esperando solicitud...</p>';
    document.getElementById('diagnostico').innerHTML = '<p>El diagnóstico aparecerá aquí una vez completada la consulta</p>';
    currentCita = null;
}

// Evento de carga inicial
window.addEventListener('load', () => {
    console.log('🚀 Sistema Médico JADE iniciado');
});