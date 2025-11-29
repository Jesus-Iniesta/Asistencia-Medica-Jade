// ========== LÓGICA DE PASOS ==========
import { CONFIG } from './config.js';
import { state, setSignosVitales, setDiagnostico, setPago } from './data.js';
import { delay, generarSignosVitales, generarFolio, getFechaHoraCompleta, determinarUrgencia, determinarCategoria } from './utils.js';
import { animarSignoVital, typeWriterEffect, showStep, updateStatus, mostrarReciboPago } from './ui.js';
import { enviarCita, pollingDiagnostico } from './api.js';

// ========== PASO 2: Signos Vitales ==========
export async function procesarSignosVitales() {
    const signos = generarSignosVitales(CONFIG.SIGNOS_VITALES);
    setSignosVitales(signos);

    // Animar signos vitales
    await animarSignoVital('temperatura', signos.temperatura + '°C', 70);
    await delay(500);
    await animarSignoVital('altura', signos.altura + ' cm', 85);
    await delay(500);
    await animarSignoVital('ritmo', signos.ritmo + ' bpm', 75);
    await delay(500);
    await animarSignoVital('presion', signos.presion + ' mmHg', 80);
    await delay(1000);

    // Mostrar confirmación
    document.getElementById('status-enfermero').innerHTML = `
        <div style="color: #10b981; font-weight: 600; display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 1.5em;">✅</span>
            <span>Signos vitales registrados correctamente</span>
        </div>
    `;

    await delay(1500);
}

// ========== PASO 3: Enviar Cita y Obtener Diagnóstico ==========
export async function procesarCita() {
    showStep('doctor');
    updateStatus('doctor', 'Doctor revisando tu caso clínico...');

    try {
        const data = await enviarCita(state.currentPacienteData);

        if (data.status === 'success') {
            updateStatus('doctor', 'El doctor está elaborando tu diagnóstico...');
            
            await pollingDiagnostico(
                state.currentPacienteData.pacienteId,
                async (diagnostico) => {
                    updateStatus('completado', 'Consulta finalizada exitosamente');
                    showStep('diagnostico');
                    await mostrarDiagnostico(diagnostico);
                    document.getElementById('btnProcederCobro').style.display = 'block';
                },
                (error) => {
                    updateStatus('error', error);
                }
            );
        }
    } catch (error) {
        console.error('Error:', error);
        updateStatus('error', 'Error al procesar la solicitud');
    }
}

// ========== PASO 4: Mostrar Diagnóstico ==========
export async function mostrarDiagnostico(data) {
    setDiagnostico(data);
    
    const urgencia = determinarUrgencia(data.diagnostico);
    const categoria = determinarCategoria(data.diagnostico);

    const diagnosticoDiv = document.getElementById('diagnostico');
    diagnosticoDiv.innerHTML = generarHTMLDiagnostico(data, urgencia, categoria);

    // Efecto de escritura
    await delay(500);
    const mensajeDoctor = `Hola ${state.currentPacienteData.nombre}, soy el ${data.doctorNombre || 'Dr. Pedro Ramírez'} y te recomiendo ${data.tratamiento || 'seguir las indicaciones mencionadas'}.`;
    await typeWriterEffect('mensaje-doctor', mensajeDoctor);
    document.getElementById('typing-indicator').style.display = 'none';
}

function generarHTMLDiagnostico(data, urgencia, categoria) {
    return `
        <div class="diagnostico-header" style="background: linear-gradient(135deg, ${urgencia.color}15, ${urgencia.color}05); 
                                                border-left: 4px solid ${urgencia.color}; 
                                                padding: 20px; border-radius: 12px; margin-bottom: 24px;">
            <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                <span style="font-size: 2em;">${urgencia.icono}</span>
                <h3 style="margin: 0; color: ${urgencia.color}; font-size: 1.3em;">
                    ${urgencia.nivel === 'urgente' ? 'Atención Urgente Requerida' : 
                      urgencia.nivel === 'importante' ? 'Atención Importante' : 'Diagnóstico Completado'}
                </h3>
            </div>
            <div style="display: flex; align-items: center; gap: 8px; color: #6b7280;">
                <span style="font-size: 1.2em;">${categoria.icono}</span>
                <span style="font-weight: 500;">Categoría: ${categoria.nombre}</span>
            </div>
        </div>

        ${generarSeccionDoctor(data)}
        ${generarSeccionMensaje()}
        ${generarSeccionDiagnostico(data)}
        ${generarSeccionTratamiento(data)}
        ${generarSeccionProximaCita(data)}
    `;
}

function generarSeccionDoctor(data) {
    return `
        <div class="diagnostico-item" style="background: linear-gradient(135deg, #8b5cf615, #ffffff); 
                                             border-left: 4px solid #8b5cf6; margin-bottom: 20px;">
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
    `;
}

function generarSeccionMensaje() {
    return `
        <div class="diagnostico-item" style="background: linear-gradient(135deg, #3b82f615, #ffffff); 
                                             border-left: 4px solid #3b82f6; margin-bottom: 20px;">
            <h4 style="color: #3b82f6; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                <span>💬</span> Mensaje del Doctor
            </h4>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
                <div id="typing-indicator" style="display: flex; align-items: center; gap: 8px; color: #6b7280; margin-bottom: 8px;">
                    <div class="typing-dots"><span></span><span></span><span></span></div>
                    <span style="font-size: 0.9em; font-style: italic;">El doctor está escribiendo...</span>
                </div>
                <p id="mensaje-doctor" style="font-size: 1.05em; line-height: 1.8; color: #1f2937; font-style: italic; min-height: 24px;"></p>
            </div>
        </div>
    `;
}

function generarSeccionDiagnostico(data) {
    return `
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
        </div>
    `;
}

function generarSeccionTratamiento(data) {
    return `
        <div class="diagnostico-item" style="background: linear-gradient(135deg, #10b98115, #ffffff); 
                                             border-left: 4px solid #10b981;">
            <h4 style="color: #10b981; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                <span>💊</span> Tratamiento Recomendado
            </h4>
            <p style="font-size: 1.05em; line-height: 1.7; color: #1f2937;">
                ${data.tratamiento || 'No especificado'}
            </p>
        </div>
    `;
}

function generarSeccionProximaCita(data) {
    return `
        <div class="diagnostico-item" style="background: linear-gradient(135deg, #f59e0b15, #ffffff); 
                                             border-left: 4px solid #f59e0b;">
            <h4 style="color: #f59e0b; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                <span>📅</span> Próxima Cita
            </h4>
            <p style="font-size: 1.05em; line-height: 1.7; color: #1f2937;">
                ${data.fechaProxima || data.proximaCita || 'No programada'}
            </p>
        </div>
    `;
}

// ========== PASO 5: Procesar Pago ==========
export function procesarPago(metodo, metodosTexto) {
    const folio = generarFolio();
    const fecha = getFechaHoraCompleta();

    setPago({
        metodo,
        metodosTexto: metodosTexto[metodo],
        folio,
        fecha
    });

    mostrarReciboPago(folio, fecha, metodosTexto[metodo]);
}

