// ========== COMUNICACIÓN CON API ==========
import { CONFIG } from './config.js';

export async function enviarCita(pacienteData) {
    try {
        console.log('🌐 Enviando cita a:', CONFIG.API_URL);
        const response = await fetch(`${CONFIG.API_URL}/cita`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                nombre: pacienteData.nombre,
                pacienteId: pacienteData.pacienteId,
                sintomas: pacienteData.sintomas
            })
        });

        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status} - ${response.statusText}`);
        }

        const data = await response.json();
        console.log('✅ Respuesta recibida:', data);
        return data;
    } catch (error) {
        console.error('❌ Error al enviar cita:', error);
        throw new Error(`No se pudo conectar con el servidor. Verifica que el servidor esté ejecutándose en ${CONFIG.API_URL}`);
    }
}

export async function obtenerDiagnostico(pacienteId) {
    try {
        const response = await fetch(`${CONFIG.API_URL}/diagnostico/${pacienteId}`);

        if (!response.ok) {
            throw new Error(`Error HTTP: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('❌ Error al obtener diagnóstico:', error);
        throw error;
    }
}

export async function pollingDiagnostico(pacienteId, onSuccess, onError) {
    let attempts = 0;
    const { MAX_ATTEMPTS, INTERVAL_MS } = CONFIG.POLLING;

    return new Promise((resolve, reject) => {
        const interval = setInterval(async () => {
            attempts++;

            try {
                const data = await obtenerDiagnostico(pacienteId);
                console.log(`Intento ${attempts}/${MAX_ATTEMPTS}:`, data);

                if (data.pacienteId && data.diagnostico && 
                    data.diagnostico !== "En proceso..." && 
                    data.diagnostico !== null) {
                    
                    clearInterval(interval);
                    onSuccess(data);
                    resolve(data);
                    
                } else if (attempts >= MAX_ATTEMPTS) {
                    clearInterval(interval);
                    onError('Tiempo de espera agotado. Por favor, verifica la conexión con el servidor.');
                    reject(new Error('Timeout'));
                }
            } catch (error) {
                console.error('Error polling:', error);
                if (attempts >= MAX_ATTEMPTS) {
                    clearInterval(interval);
                    onError('Error de conexión con el servidor. Verifica que el servidor JADE esté activo.');
                    reject(error);
                }
            }
        }, INTERVAL_MS);
    });
}
