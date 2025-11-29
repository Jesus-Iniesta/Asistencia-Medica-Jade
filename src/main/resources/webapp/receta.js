// ========== GENERACIÓN DE RECETA MÉDICA ==========
import { state } from './data.js';
import { getFechaActual, getHoraActual } from './utils.js';

export function generarRecetaMedica() {
    const recetaContainer = document.getElementById('receta-container');
    const fecha = getFechaActual();
    const hora = getHoraActual();

    const { paciente: p, signosVitales: s, diagnostico: d, pago } = state.datosCompletosConsulta;

    recetaContainer.innerHTML = `
        ${generarEncabezado(pago, fecha, hora)}
        ${generarDatosPaciente(p, fecha, hora)}
        ${generarSignosVitales(s)}
        ${generarMotivoConsulta(p)}
        ${generarMedicoTratante(d)}
        ${generarDiagnostico(d)}
        ${generarTratamiento(d)}
        ${generarProximaCita(d)}
        ${generarPieFirma(d)}
        ${generarPieDocumento(fecha, hora)}
    `;
}

function generarEncabezado(pago, fecha, hora) {
    return `
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
    `;
}

function generarDatosPaciente(p, fecha, hora) {
    return `
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
    `;
}

function generarSignosVitales(s) {
    return `
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
    `;
}

function generarMotivoConsulta(p) {
    return `
        <div style="background: linear-gradient(135deg, #f59e0b15, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #f59e0b;">
            <h3 style="color: #f59e0b; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">📋 MOTIVO DE CONSULTA</h3>
            <p style="margin: 0; line-height: 1.6; color: #1f2937; background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb;">${p.sintomas}</p>
        </div>
    `;
}

function generarMedicoTratante(d) {
    return `
        <div style="background: linear-gradient(135deg, #8b5cf615, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #8b5cf6;">
            <h3 style="color: #8b5cf6; margin-top: 0; margin-bottom: 16px; font-size: 1.2em;">👨‍⚕️ MÉDICO TRATANTE</h3>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
                <p style="margin: 0; font-size: 1.2em; font-weight: 600; color: #1f2937;">${d.doctorNombre || 'Dr. Pedro Ramírez'}</p>
                <p style="margin: 4px 0 0 0; color: #6b7280;">🎓 ${d.doctorEspecialidad || 'Medicina General'}</p>
            </div>
        </div>
    `;
}

function generarDiagnostico(d) {
    return `
        <div style="background: linear-gradient(135deg, #ef444415, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #ef4444;">
            <h3 style="color: #ef4444; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">🔍 DIAGNÓSTICO MÉDICO</h3>
            <p style="margin: 0; line-height: 1.6; color: #1f2937; background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb; font-weight: 500;">${d.diagnostico}</p>
        </div>
    `;
}

function generarTratamiento(d) {
    return `
        <div style="background: linear-gradient(135deg, #06b6d415, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #06b6d4;">
            <h3 style="color: #06b6d4; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">💊 TRATAMIENTO PRESCRITO</h3>
            <div style="background: white; padding: 16px; border-radius: 8px; border: 1px solid #e5e7eb;">
                <p style="margin: 0; line-height: 1.8; color: #1f2937; white-space: pre-line;">${d.tratamiento}</p>
            </div>
        </div>
    `;
}

function generarProximaCita(d) {
    return `
        <div style="background: linear-gradient(135deg, #10b98115, #ffffff); padding: 20px; border-radius: 12px; margin-bottom: 24px; border-left: 4px solid #10b981;">
            <h3 style="color: #10b981; margin-top: 0; margin-bottom: 12px; font-size: 1.2em;">📅 PRÓXIMA CITA</h3>
            <p style="margin: 0; line-height: 1.6; color: #1f2937; background: white; padding: 12px; border-radius: 8px; border: 1px solid #e5e7eb; font-size: 1.1em; font-weight: 500;">${d.fechaProxima || d.proximaCita || 'No programada'}</p>
        </div>
    `;
}

function generarPieFirma(d) {
    return `
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
    `;
}

function generarPieDocumento(fecha, hora) {
    return `
        <div style="text-align: center; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e5e7eb;">
            <p style="margin: 0; color: #9ca3af; font-size: 0.8em;">
                Sistema de Atención Médica con Agentes Inteligentes JADE<br>
                Generado electrónicamente el ${fecha} a las ${hora}
            </p>
        </div>
    `;
}

