const API_URL = 'http://localhost:7070/api';

let currentCita = null;
let currentPacienteData = null;

// 🔥 Variable global para almacenar datos completos (incluyendo signos vitales)
let datosCompletosConsulta = {
    paciente: null,
    signosVitales: null,
    diagnostico: null,
    pago: null
};

// ========== PASO 0: Botón Inicial ==========
document.getElementById('btnPasarRecepcion').addEventListener('click', () => {
    hideAllSteps();
    document.getElementById('step-recepcion').style.display = 'block';
    scrollToTop();
});

// ========== PASO 1: Registro en Recepción ==========
document.getElementById('recepcionForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    currentPacienteData = {
        nombre: document.getElementById('nombre').value,
        edad: document.getElementById('edad').value,
        genero: document.getElementById('genero').value,
        sintomas: document.getElementById('sintomas').value,
        pacienteId: generarIDUnico()
    };

    // 🔥 GUARDAR datos del paciente para la receta
    datosCompletosConsulta.paciente = currentPacienteData;

    // Pasar al siguiente paso: Signos vitales
    hideAllSteps();
    document.getElementById('step-enfermero').style.display = 'block';
    scrollToTop();

    // Simular toma de signos vitales
    await simularSignosVitales();

    // Enviar cita al backend
    await enviarCitaAlSistema();
});

// ========== PASO 2: Simulación de Signos Vitales ==========
async function simularSignosVitales() {
    const signos = {
        temperatura: (35.5 + Math.random() * 2.5).toFixed(1), // 35.5 - 38.0
        altura: (150 + Math.random() * 40).toFixed(0), // 150 - 190 cm
        ritmo: (60 + Math.random() * 40).toFixed(0), // 60 - 100 bpm
        presion: `${(110 + Math.random() * 20).toFixed(0)}/${(70 + Math.random() * 20).toFixed(0)}`
    };

    // 🔥 GUARDAR signos vitales para la receta
    datosCompletosConsulta.signosVitales = signos;

    // Animar cada signo vital
    await animarSignoVital('temperatura', signos.temperatura + '°C', 70);
    await delay(500);
    await animarSignoVital('altura', signos.altura + ' cm', 85);
    await delay(500);
    await animarSignoVital('ritmo', signos.ritmo + ' bpm', 75);
    await delay(500);
    await animarSignoVital('presion', signos.presion + ' mmHg', 80);
    await delay(1000);

    // Cambiar mensaje de status
    const statusEnfermero = document.getElementById('status-enfermero');
    statusEnfermero.innerHTML = `
        <div style="color: #10b981; font-weight: 600; display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 1.5em;">✅</span>
            <span>Signos vitales registrados correctamente</span>
        </div>
    `;

    await delay(1500);
}

async function animarSignoVital(tipo, valor, porcentaje) {
    const valorElement = document.getElementById(`valor-${tipo}`);
    const progressElement = document.getElementById(`progress-${tipo}`);

    // Animar valor
    valorElement.textContent = valor;
    valorElement.style.animation = 'fadeIn 0.5s ease-out';

    // Animar barra de progreso
    progressElement.style.width = '0%';
    await delay(100);
    progressElement.style.width = porcentaje + '%';
}

function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// ========== PASO 3: Enviar Cita al Sistema JADE ==========
async function enviarCitaAlSistema() {
    hideAllSteps();
    document.getElementById('step-doctor').style.display = 'block';
    scrollToTop();

    updateStatus('doctor', 'Doctor revisando tu caso clínico...');

    try {
        const response = await fetch(`${API_URL}/cita`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nombre: currentPacienteData.nombre,
                pacienteId: currentPacienteData.pacienteId,
                sintomas: currentPacienteData.sintomas
            })
        });

        const data = await response.json();

        if (data.status === 'success') {
            updateStatus('doctor', 'El doctor está elaborando tu diagnóstico...');

            // Iniciar polling para obtener el diagnóstico
            await startPollingDiagnostico(currentPacienteData.pacienteId);
        }
    } catch (error) {
        console.error('Error:', error);
        updateStatus('error', 'Error al procesar la solicitud');
    }
}

function updateStatus(step, message) {
    const estadoDiv = document.getElementById('estado');

    const icons = {
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
        recepcion: 'En Recepción',
        enfermero: 'Con Enfermero',
        doctor: 'Consulta Médica',
        completado: 'Consulta Completada',
        error: 'Error en el Proceso'
    };
    return titles[step] || step;
}

// ========== PASO 4: Polling del Diagnóstico ==========
async function startPollingDiagnostico(pacienteId) {
    let attempts = 0;
    const maxAttempts = 20;

    const interval = setInterval(async () => {
        attempts++;

        try {
            const response = await fetch(`${API_URL}/diagnostico/${pacienteId}`);
            const data = await response.json();

            console.log(`Intento ${attempts}:`, data);

            if (data.pacienteId && data.diagnostico &&
                data.diagnostico !== "En proceso..." &&
                data.diagnostico !== null) {

                clearInterval(interval);
                updateStatus('completado', 'Consulta finalizada exitosamente');

                // Mostrar diagnóstico
                hideAllSteps();
                document.getElementById('step-diagnostico').style.display = 'block';
                scrollToTop();

                await displayDiagnostico(data);

                // Mostrar botón de proceder al cobro
                document.getElementById('btnProcederCobro').style.display = 'block';

            } else if (attempts >= maxAttempts) {
                clearInterval(interval);
                updateStatus('error', 'Tiempo de espera agotado');
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

// ========== PASO 4: Mostrar Diagnóstico ==========
async function displayDiagnostico(data) {
    const diagnosticoDiv = document.getElementById('diagnostico');

    // Determinar urgencia y categoría
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

    // Determinar categoría
    let categoria = 'General';
    let iconoCategoria = '🩺';

    if (data.diagnostico.includes('respiratori') || data.diagnostico.includes('Tos') ||
        data.diagnostico.includes('Faringitis') || data.diagnostico.includes('Bronquitis')) {
        categoria = 'Respiratorio';
        iconoCategoria = '🫁';
    } else if (data.diagnostico.includes('Gastro') || data.diagnostico.includes('Diarrea') ||
               data.diagnostico.includes('estómago') || data.diagnostico.includes('náuseas') ||
               data.diagnostico.includes('Dispepsia') || data.diagnostico.includes('Estreñimiento')) {
        categoria = 'Gastrointestinal';
        iconoCategoria = '🫃';
    } else if (data.diagnostico.includes('Cefalea') || data.diagnostico.includes('Migraña')) {
        categoria = 'Neurológico';
        iconoCategoria = '🧠';
    } else if (data.diagnostico.includes('cardio') || data.diagnostico.includes('presión') ||
               data.diagnostico.includes('pecho')) {
        categoria = 'Cardiovascular';
        iconoCategoria = '❤️';
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

        <div class="diagnostico-item" style="background: linear-gradient(135deg, #3b82f615, #ffffff); 
                                             border-left: 4px solid #3b82f6; margin-bottom: 20px;">
            <h4 style="color: #3b82f6; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                <span>💬</span> Mensaje del Doctor
            </h4>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
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
                    <span>💊</span> Tratamiento Recomendado
                </h4>
                <p style="font-size: 1.05em; line-height: 1.7; color: #1f2937;">
                    ${data.tratamiento || 'No especificado'}
                </p>
            </div>

            <div class="diagnostico-item" style="background: linear-gradient(135deg, #f59e0b15, #ffffff); 
                                                 border-left: 4px solid #f59e0b;">
                <h4 style="color: #f59e0b; font-size: 1.1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
                    <span>📅</span> Próxima Cita
                </h4>
                <p style="font-size: 1.05em; line-height: 1.7; color: #1f2937;">
                    ${data.fechaProxima || data.proximaCita || 'No programada'}
                </p>
            </div>
        </div>
    `;

    diagnosticoDiv.innerHTML = html;

    // 🔥 GUARDAR diagnóstico para la receta
    datosCompletosConsulta.diagnostico = data;

    // Efecto de escritura para el mensaje del doctor
    await delay(500);
    const mensajeDoctor = `Hola ${currentPacienteData.nombre}, soy el ${data.doctorNombre || 'Dr. Pedro Ramírez'} y te recomiendo ${data.tratamiento || 'seguir las indicaciones mencionadas'}.`;
    await typeWriterEffect('mensaje-doctor', mensajeDoctor);

    // Ocultar indicador de escritura
    document.getElementById('typing-indicator').style.display = 'none';
}

// Efecto de escritura tipo IA
async function typeWriterEffect(elementId, text) {
    const element = document.getElementById(elementId);
    element.textContent = '';

    for (let i = 0; i < text.length; i++) {
        element.textContent += text.charAt(i);
        await delay(30); // 30ms por carácter
    }
}

// ========== PASO 5: Proceder al Cobro ==========
document.getElementById('btnProcederCobro').addEventListener('click', () => {
    hideAllSteps();
    document.getElementById('step-cobro').style.display = 'block';
    document.getElementById('cobro-nombre').textContent = currentPacienteData.nombre;
    scrollToTop();
});

// Manejo de métodos de pago
document.querySelectorAll('.btn-pago').forEach(btn => {
    btn.addEventListener('click', function() {
        const metodo = this.getAttribute('data-metodo');
        procesarPago(metodo);
    });
});

function procesarPago(metodo) {
    const metodosTexto = {
        'efectivo': '💵 Efectivo',
        'tarjeta': '💳 Tarjeta de Crédito/Débito',
        'transferencia': '🏦 Transferencia Bancaria'
    };

    // Generar folio aleatorio
    const folio = 'F' + Date.now().toString().slice(-8);
    const fecha = new Date().toLocaleString('es-MX', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });

    // 🔥 GUARDAR información del pago para la receta
    datosCompletosConsulta.pago = {
        metodo: metodo,
        metodosTexto: metodosTexto[metodo],
        folio: folio,
        fecha: fecha
    };

    document.getElementById('metodo-seleccionado').textContent = metodosTexto[metodo];
    document.getElementById('fecha-pago').textContent = fecha;
    document.getElementById('folio-pago').textContent = folio;

    // Ocultar botones de pago y mostrar recibo
    document.querySelector('.metodo-pago').style.display = 'none';
    document.querySelector('.resumen-cobro').style.opacity = '0.6';
    document.getElementById('recibo-pago').style.display = 'block';
}

// ========== Nueva Consulta ==========
document.getElementById('btnNuevaConsulta').addEventListener('click', () => {
    // Resetear todo
    currentCita = null;
    currentPacienteData = null;
    datosCompletosConsulta = {
        paciente: null,
        signosVitales: null,
        diagnostico: null,
        pago: null
    };

    document.getElementById('recepcionForm').reset();
    document.querySelector('.metodo-pago').style.display = 'block';
    document.querySelector('.resumen-cobro').style.opacity = '1';
    document.getElementById('recibo-pago').style.display = 'none';

    hideAllSteps();
    document.getElementById('step-inicio').style.display = 'block';
    scrollToTop();
});

// ========== PASO 6: Ver Receta Médica ==========
document.getElementById('btnVerReceta').addEventListener('click', () => {
    generarRecetaMedica();
    hideAllSteps();
    document.getElementById('step-receta').style.display = 'block';
    scrollToTop();
});

document.getElementById('btnVolverInicio').addEventListener('click', () => {
    // Resetear todo
    currentCita = null;
    currentPacienteData = null;
    datosCompletosConsulta = {
        paciente: null,
        signosVitales: null,
        diagnostico: null,
        pago: null
    };

    document.getElementById('recepcionForm').reset();
    document.querySelector('.metodo-pago').style.display = 'block';
    document.querySelector('.resumen-cobro').style.opacity = '1';
    document.getElementById('recibo-pago').style.display = 'none';

    hideAllSteps();
    document.getElementById('step-inicio').style.display = 'block';
    scrollToTop();
});

document.getElementById('btnImprimirReceta').addEventListener('click', () => {
    window.print();
});

function generarRecetaMedica() {
    const recetaContainer = document.getElementById('receta-container');
    const fecha = new Date().toLocaleDateString('es-MX', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
    const hora = new Date().toLocaleTimeString('es-MX', {
        hour: '2-digit',
        minute: '2-digit'
    });

    const p = datosCompletosConsulta.paciente;
    const s = datosCompletosConsulta.signosVitales;
    const d = datosCompletosConsulta.diagnostico;
    const pago = datosCompletosConsulta.pago;

    const recetaHTML = `
        <div class="receta-header">
            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 24px; padding-bottom: 16px; border-bottom: 3px solid #4F46E5;">
                <div>
                    <h1 style="color: #4F46E5; margin: 0; font-size: 1.8em;">🏥 Centro Médico JADE</h1>
                    <p style="margin: 4px 0; color: #6b7280; font-size: 0.95em;">Sistema de Atención Médica Distribuida</p>
                    <p style="margin: 2px 0; color: #9ca3af; font-size: 0.85em;">📍 Av. Principal #123, Ciudad</p>
                    <p style="margin: 2px 0; color: #9ca3af; font-size: 0.85em;">📞 Tel: (555) 123-4567</p>
                </div>
                <div style="text-align: right;">
                    <p style="margin: 2px 0; color: #1f2937; font-weight: 600;">Folio: ${pago.folio}</p>
                    <p style="margin: 2px 0; color: #6b7280; font-size: 0.9em;">📅 ${fecha}</p>
                    <p style="margin: 2px 0; color: #6b7280; font-size: 0.9em;">🕐 ${hora}</p>
                </div>
            </div>
        </div>

        <div style="background: linear-gradient(135deg, #4F46E515, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #4F46E5;">
            <h3 style="color: #4F46E5; margin-top: 0; margin-bottom: 16px; font-size: 1.2em;">👤 DATOS DEL PACIENTE</h3>
            <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;">
                <div>
                    <p style="margin: 4px 0;"><strong>Nombre:</strong> ${p.nombre}</p>
                    <p style="margin: 4px 0;"><strong>Edad:</strong> ${p.edad} años</p>
                    <p style="margin: 4px 0;"><strong>Género:</strong> ${p.genero}</p>
                </div>
                <div>
                    <p style="margin: 4px 0;"><strong>ID Paciente:</strong> ${p.pacienteId}</p>
                    <p style="margin: 4px 0;"><strong>Fecha de Consulta:</strong> ${fecha}</p>
                    <p style="margin: 4px 0;"><strong>Hora:</strong> ${hora}</p>
                </div>
            </div>
        </div>

        <div style="background: linear-gradient(135deg, #10b98115, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #10b981;">
            <h3 style="color: #10b981; margin-top: 0; margin-bottom: 16px; font-size: 1.2em;">🩺 SIGNOS VITALES</h3>
            <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;">
                <div style="background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb;">
                    <p style="margin: 0; color: #6b7280; font-size: 0.9em;">🌡️ Temperatura</p>
                    <p style="margin: 4px 0 0 0; font-size: 1.3em; font-weight: 600; color: #1f2937;">${s.temperatura}°C</p>
                </div>
                <div style="background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb;">
                    <p style="margin: 0; color: #6b7280; font-size: 0.9em;">🩺 Presión Arterial</p>
                    <p style="margin: 4px 0 0 0; font-size: 1.3em; font-weight: 600; color: #1f2937;">${s.presion} mmHg</p>
                </div>
                <div style="background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb;">
                    <p style="margin: 0; color: #6b7280; font-size: 0.9em;">❤️ Frecuencia Cardíaca</p>
                    <p style="margin: 4px 0 0 0; font-size: 1.3em; font-weight: 600; color: #1f2937;">${s.ritmo} bpm</p>
                </div>
                <div style="background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb;">
                    <p style="margin: 0; color: #6b7280; font-size: 0.9em;">📏 Altura</p>
                    <p style="margin: 4px 0 0 0; font-size: 1.3em; font-weight: 600; color: #1f2937;">${s.altura} cm</p>
                </div>
            </div>
        </div>

        <div style="background: linear-gradient(135deg, #f59e0b15, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #f59e0b;">
            <h3 style="color: #f59e0b; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">📋 MOTIVO DE CONSULTA</h3>
            <p style="margin: 0; line-height: 1.6; color: #1f2937; background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb;">${p.sintomas}</p>
        </div>

        <div style="background: linear-gradient(135deg, #8b5cf615, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #8b5cf6;">
            <h3 style="color: #8b5cf6; margin-top: 0; margin-bottom: 16px; font-size: 1.2em;">👨‍⚕️ MÉDICO TRATANTE</h3>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
                <p style="margin: 0; font-size: 1.2em; font-weight: 600; color: #1f2937;">${d.doctorNombre || 'Dr. Pedro Ramírez'}</p>
                <p style="margin: 4px 0 0 0; color: #6b7280;">🎓 ${d.doctorEspecialidad || 'Medicina General'}</p>
            </div>
        </div>

        <div style="background: linear-gradient(135deg, #ef444415, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #ef4444;">
            <h3 style="color: #ef4444; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">🔍 DIAGNÓSTICO MÉDICO</h3>
            <p style="margin: 0; line-height: 1.6; color: #1f2937; background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb; font-weight: 500;">${d.diagnostico}</p>
        </div>

        <div style="background: linear-gradient(135deg, #06b6d415, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #06b6d4;">
            <h3 style="color: #06b6d4; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">💊 TRATAMIENTO PRESCRITO</h3>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
                <p style="margin: 0; line-height: 1.8; color: #1f2937; white-space: pre-line;">${d.tratamiento}</p>
            </div>
        </div>

        <div style="background: linear-gradient(135deg, #10b98115, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #10b981;">
            <h3 style="color: #10b981; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">📅 PRÓXIMA CITA</h3>
            <p style="margin: 0; line-height: 1.6; color: #1f2937; background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb; font-size: 1.1em; font-weight: 500;">${d.fechaProxima || d.proximaCita || 'No programada'}</p>
        </div>

        <div style="border-top: 2px dashed #d1d5db; padding-top: 20px; margin-top: 24px;">
            <div style="display: flex; justify-content: space-between; align-items: end;">
                <div>
                    <p style="margin: 0; color: #6b7280; font-size: 0.85em;">⚠️ Esta receta es válida por 30 días</p>
                    <p style="margin: 4px 0 0 0; color: #6b7280; font-size: 0.85em;">📞 Para dudas o emergencias: (555) 123-4567</p>
                </div>
                <div style="text-align: center; padding: 0 40px;">
                    <div style="border-top: 2px solid #1f2937; padding-top: 8px; min-width: 200px;">
                        <p style="margin: 0; font-weight: 600; color: #1f2937;">${d.doctorNombre || 'Dr. Pedro Ramírez'}</p>
                        <p style="margin: 4px 0 0 0; color: #6b7280; font-size: 0.9em;">Cédula Profesional: 1234567</p>
                    </div>
                </div>
            </div>
        </div>

        <div style="text-align: center; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e5e7eb;">
            <p style="margin: 0; color: #9ca3af; font-size: 0.8em;">
                Sistema de Atención Médica con Agentes Inteligentes JADE<br>
                Generado electrónicamente el ${fecha} a las ${hora}
            </p>
        </div>
    `;

    recetaContainer.innerHTML = recetaHTML;
}

// ========== Utilidades ==========
function hideAllSteps() {
    document.getElementById('step-inicio').style.display = 'none';
    document.getElementById('step-recepcion').style.display = 'none';
    document.getElementById('step-enfermero').style.display = 'none';
    document.getElementById('step-doctor').style.display = 'none';
    document.getElementById('step-diagnostico').style.display = 'none';
    document.getElementById('step-cobro').style.display = 'none';
    document.getElementById('step-receta').style.display = 'none';
    document.getElementById('btnProcederCobro').style.display = 'none';
}

function scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function generarIDUnico() {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 1000);
    return `P${timestamp}${random}`;
}
