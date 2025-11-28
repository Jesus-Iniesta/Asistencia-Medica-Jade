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

    // Determinar el nivel de urgencia basado en el diagnóstico
    let urgencia = 'normal';
    let iconoUrgencia = '✅';
    let colorUrgencia = '#10b981';

    if (data.diagnostico.includes('URGENTE') || data.diagnostico.includes('🚨')) {
        urgencia = 'urgente';
        iconoUrgencia = '🚨';
        colorUrgencia = '#ef4444';
    } else if (data.diagnostico.includes('Requiere atención') || data.diagnostico.includes('⚠️')) {
        urgencia = 'importante';
        iconoUrgencia = '⚠️';
        colorUrgencia = '#f59e0b';
    }

    // Extraer categoría del diagnóstico
    let categoria = 'General';
    let iconoCategoria = '🩺';

    if (data.diagnostico.includes('respiratori') || data.diagnostico.includes('Tos') ||
        data.diagnostico.includes('Faringitis') || data.diagnostico.includes('Bronquitis')) {
        categoria = 'Respiratorio';
        iconoCategoria = '🫁';
    } else if (data.diagnostico.includes('Gastro') || data.diagnostico.includes('Diarrea') ||
               data.diagnostico.includes('estómago') || data.diagnostico.includes('náuseas')) {
        categoria = 'Gastrointestinal';
        iconoCategoria = '🫃';
    } else if (data.diagnostico.includes('Cefalea') || data.diagnostico.includes('Migraña') ||
               data.diagnostico.includes('vértigo')) {
        categoria = 'Neurológico';
        iconoCategoria = '🧠';
    } else if (data.diagnostico.includes('cardio') || data.diagnostico.includes('presión') ||
               data.diagnostico.includes('Hipertensión') || data.diagnostico.includes('pecho')) {
        categoria = 'Cardiovascular';
        iconoCategoria = '❤️';
    } else if (data.diagnostico.includes('Mialgia') || data.diagnostico.includes('Artralgia') ||
               data.diagnostico.includes('muscular')) {
        categoria = 'Musculoesquelético';
        iconoCategoria = '💪';
    } else if (data.diagnostico.includes('cutáne') || data.diagnostico.includes('Erupción') ||
               data.diagnostico.includes('alérgica')) {
        categoria = 'Dermatológico';
        iconoCategoria = '🧴';
    }

    const html = `
        <div class="diagnostico-header" style="background: linear-gradient(135deg, ${colorUrgencia}15, ${colorUrgencia}05); 
                                                border-left: 4px solid ${colorUrgencia}; 
                                                padding: 20px; 
                                                border-radius: 12px; 
                                                margin-bottom: 24px;">
            <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                <span style="font-size: 2em;">${iconoUrgencia}</span>
                <h3 style="margin: 0; color: ${colorUrgencia}; font-size: 1.3em;">
                    ${urgencia === 'urgente' ? 'Atención Urgente Requerida' : 
                      urgencia === 'importante' ? 'Atención Importante' : 
                      'Diagnóstico Completado'}
                </h3>
            </div>
            <div style="display: flex; align-items: center; gap: 8px; color: #6b7280;">
                <span style="font-size: 1.2em;">${iconoCategoria}</span>
                <span style="font-weight: 500;">Categoría: ${categoria}</span>
            </div>
        </div>

        <!-- Información del Doctor Especialista -->
        <div class="diagnostico-item" style="background: linear-gradient(135deg, #8b5cf615, #ffffff); 
                                             border-left: 4px solid #8b5cf6;
                                             margin-bottom: 20px;">
            <h4 style="color: #8b5cf6; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                <span>👨‍⚕️</span> Doctor Tratante
            </h4>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
                <p style="font-size: 1.15em; font-weight: 600; color: #1f2937; margin-bottom: 6px;">
                    ${data.doctorNombre || 'Dr. Pedro Ramírez'}
                </p>
                <p style="font-size: 0.95em; color: #6b7280; margin: 0;">
                    🎓 ${data.doctorEspecialidad || 'Medicina General'}
                </p>
            </div>
        </div>

        <!-- 🔥 NUEVO: Mensaje Personalizado del Doctor con Efecto Generativo -->
        <div class="diagnostico-item" style="background: linear-gradient(135deg, #3b82f615, #ffffff); 
                                             border-left: 4px solid #3b82f6;
                                             margin-bottom: 20px;">
            <h4 style="color: #3b82f6; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                <span>💬</span> Mensaje del Doctor
            </h4>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb; position: relative;">
                <div id="typing-indicator" style="display: flex; align-items: center; gap: 8px; color: #6b7280; margin-bottom: 8px;">
                    <div class="typing-dots">
                        <span></span><span></span><span></span>
                    </div>
                    <span style="font-size: 0.9em; font-style: italic;">El doctor está escribiendo...</span>
                </div>
                <p id="mensaje-doctor" style="font-size: 1.05em; line-height: 1.8; color: #1f2937; font-style: italic; min-height: 24px;">
                </p>
            </div>
        </div>
        
        <div class="diagnostico-content">
            <div class="diagnostico-item" style="background: linear-gradient(135deg, #4F46E515, #ffffff); 
                                                 border-left: 4px solid #4F46E5;">
                <h4 style="color: #4F46E5; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                    <span>🔍</span> Diagnóstico Médico
                </h4>
                <p style="font-size: 1.1em; line-height: 1.7; color: #1f2937; font-weight: 500;">
                    ${data.diagnostico}
                </p>
            </div>
            
            <div class="diagnostico-item" style="background: linear-gradient(135deg, #10b98115, #ffffff); 
                                                 border-left: 4px solid #10b981;">
                <h4 style="color: #10b981; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                    <span>💊</span> Plan de Tratamiento
                </h4>
                <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
                    <p style="font-size: 1.05em; line-height: 1.8; color: #374151; white-space: pre-line;">
                        ${formatearTratamiento(data.tratamiento)}
                    </p>
                </div>
            </div>
            
            <div class="diagnostico-item" style="background: linear-gradient(135deg, #f59e0b15, #ffffff); 
                                                 border-left: 4px solid #f59e0b;">
                <h4 style="color: #f59e0b; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                    <span>📅</span> Seguimiento Médico
                </h4>
                <p style="font-size: 1.1em; line-height: 1.7; color: #1f2937; font-weight: 500;">
                    ${data.fechaProxima}
                </p>
            </div>

            ${urgencia === 'urgente' ? `
            <div class="diagnostico-item" style="background: #fee2e2; border-left: 4px solid #ef4444; border: 2px solid #ef4444;">
                <h4 style="color: #dc2626; font-size: 1.1em; margin-bottom: 8px; display: flex; align-items: center; gap: 8px;">
                    <span>⚠️</span> Importante
                </h4>
                <p style="font-size: 1em; color: #991b1b; font-weight: 600;">
                    Por favor, siga las indicaciones de inmediato y acuda al servicio de urgencias más cercano.
                </p>
            </div>
            ` : ''}
        </div>
        
        <button onclick="resetForm()" class="btn-primary" style="margin-top: 24px;">
            <span style="font-size: 1.2em;">🔄</span> Nueva Consulta
        </button>
    `;

    diagnosticoDiv.innerHTML = html;

    // 🔥 NUEVO: Iniciar efecto de escritura generativa para el mensaje del doctor
    setTimeout(() => {
        mostrarMensajeGenerativo(data.mensajeDoctor || `Hola ${data.nombrePaciente || 'paciente'}, soy ${data.doctorNombre}. He revisado tu caso y te recomiendo seguir el tratamiento indicado.`);
    }, 500);
}

// 🔥 NUEVO: Función para mostrar texto con efecto generativo (como IA)
function mostrarMensajeGenerativo(mensaje) {
    const mensajeElement = document.getElementById('mensaje-doctor');
    const typingIndicator = document.getElementById('typing-indicator');

    if (!mensajeElement || !typingIndicator) return;

    let index = 0;
    const velocidad = 30; // Milisegundos por carácter

    // Mostrar indicador de escritura por 1 segundo
    setTimeout(() => {
        typingIndicator.style.display = 'none';

        // Iniciar efecto de escritura
        const interval = setInterval(() => {
            if (index < mensaje.length) {
                mensajeElement.textContent += mensaje.charAt(index);
                index++;
            } else {
                clearInterval(interval);
                // Agregar cursor parpadeante al final
                mensajeElement.innerHTML += '<span style="animation: blink 1s infinite;">|</span>';
            }
        }, velocidad);
    }, 1000);
}

function formatearTratamiento(tratamiento) {
    // Dividir el tratamiento en puntos si contiene comas
    if (tratamiento.includes(',')) {
        const puntos = tratamiento.split(',').map(punto => punto.trim());
        return puntos.map((punto, index) =>
            `${index + 1}. ${punto.charAt(0).toUpperCase() + punto.slice(1)}`
        ).join('\n');
    }

    // Si contiene punto, separar por punto
    if (tratamiento.includes('.')) {
        const puntos = tratamiento.split('.').filter(p => p.trim().length > 0).map(p => p.trim());
        return puntos.map((punto, index) =>
            `${index + 1}. ${punto.charAt(0).toUpperCase() + punto.slice(1)}`
        ).join('\n');
    }

    return tratamiento;
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