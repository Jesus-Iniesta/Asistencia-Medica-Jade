// ========== COMUNICACIÓN CON API ==========
import { CONFIG } from './config.js';

export async function enviarCita(pacienteData) {
    const response = await fetch(`${CONFIG.API_URL}/cita`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            nombre: pacienteData.nombre,
            pacienteId: pacienteData.pacienteId,
            sintomas: pacienteData.sintomas
        })
    });
    return await response.json();
}

export async function obtenerDiagnostico(pacienteId) {
    const response = await fetch(`${CONFIG.API_URL}/diagnostico/${pacienteId}`);
    return await response.json();
}

export async function pollingDiagnostico(pacienteId, onSuccess, onError) {
    let attempts = 0;
    const { MAX_ATTEMPTS, INTERVAL_MS } = CONFIG.POLLING;

    return new Promise((resolve, reject) => {
        const interval = setInterval(async () => {
            attempts++;

            try {
                const data = await obtenerDiagnostico(pacienteId);
                console.log(`Intento ${attempts}:`, data);

                if (data.pacienteId && data.diagnostico && 
                    data.diagnostico !== "En proceso..." && 
                    data.diagnostico !== null) {
                    
                    clearInterval(interval);
                    onSuccess(data);
                    resolve(data);
                    
                } else if (attempts >= MAX_ATTEMPTS) {
                    clearInterval(interval);
                    onError('Tiempo de espera agotado');
                    reject(new Error('Timeout'));
                }
            } catch (error) {
                console.error('Error polling:', error);
                if (attempts >= MAX_ATTEMPTS) {
                    clearInterval(interval);
                    onError('Error al obtener diagnóstico');
                    reject(error);
                }
            }
        }, INTERVAL_MS);
    });
}

