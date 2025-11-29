// ========== FUNCIONES DE UI ==========
import { delay } from './utils.js';
import { ICONS, STEP_TITLES } from './config.js';

export function hideAllSteps() {
    const steps = ['inicio', 'recepcion', 'enfermero', 'doctor', 'diagnostico', 'cobro', 'receta'];
    steps.forEach(step => {
        document.getElementById(`step-${step}`).style.display = 'none';
    });
    document.getElementById('btnProcederCobro').style.display = 'none';
}

export function showStep(stepName) {
    hideAllSteps();
    document.getElementById(`step-${stepName}`).style.display = 'block';
    scrollToTop();
}

export function scrollToTop() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

export function updateStatus(step, message) {
    const estadoDiv = document.getElementById('estado');
    const html = `
        <div class="status-step">
            <div class="icon">${ICONS[step]}</div>
            <div class="text">
                <h3>${STEP_TITLES[step] || step}</h3>
                <p>${message}</p>
            </div>
            ${step !== 'completado' && step !== 'error' ? '<div class="loading"></div>' : ''}
        </div>
    `;
    estadoDiv.innerHTML = html;
}

export async function animarSignoVital(tipo, valor, porcentaje) {
    const valorElement = document.getElementById(`valor-${tipo}`);
    const progressElement = document.getElementById(`progress-${tipo}`);

    valorElement.textContent = valor;
    valorElement.style.animation = 'fadeIn 0.5s ease-out';

    progressElement.style.width = '0%';
    await delay(100);
    progressElement.style.width = porcentaje + '%';
}

export async function typeWriterEffect(elementId, text, speed = 30) {
    const element = document.getElementById(elementId);
    element.textContent = '';

    for (let i = 0; i < text.length; i++) {
        element.textContent += text.charAt(i);
        await delay(speed);
    }
}

export function mostrarReciboPago(folio, fecha, metodoTexto) {
    document.getElementById('metodo-seleccionado').textContent = metodoTexto;
    document.getElementById('fecha-pago').textContent = fecha;
    document.getElementById('folio-pago').textContent = folio;

    document.querySelector('.metodo-pago').style.display = 'none';
    document.querySelector('.resumen-cobro').style.opacity = '0.6';
    document.getElementById('recibo-pago').style.display = 'block';
}

export function resetFormularioCobro() {
    document.querySelector('.metodo-pago').style.display = 'block';
    document.querySelector('.resumen-cobro').style.opacity = '1';
    document.getElementById('recibo-pago').style.display = 'none';
}

