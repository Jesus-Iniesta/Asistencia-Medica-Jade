// ========== CONFIGURACIÓN ==========

// 🌐 Detectar automáticamente la URL del servidor
// Si se accede desde otro dispositivo, usa la IP del servidor
// Si se accede localmente, usa localhost
function getApiUrl() {
    // Si estamos en el mismo servidor (localhost o 127.0.0.1)
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        return 'http://localhost:7070/api';
    }
    // Si accedemos desde otro dispositivo, usa la IP del servidor
    return `http://${window.location.hostname}:7070/api`;
}

export const CONFIG = {
    API_URL: getApiUrl(),
    POLLING: {
        MAX_ATTEMPTS: 20,
        INTERVAL_MS: 2000
    },
    ANIMATION: {
        TYPING_SPEED_MS: 30,
        DELAY_MS: 500
    },
    SIGNOS_VITALES: {
        TEMPERATURA: { min: 35.5, max: 38.0 },
        ALTURA: { min: 150, max: 190 },
        RITMO: { min: 60, max: 100 },
        PRESION_SYS: { min: 110, max: 130 },
        PRESION_DIA: { min: 70, max: 90 }
    }
};

export const ICONS = {
    recepcion: '📋',
    enfermero: '💉',
    doctor: '👨‍⚕️',
    completado: '✅',
    error: '❌'
};

export const STEP_TITLES = {
    recepcion: 'En Recepción',
    enfermero: 'Con Enfermero',
    doctor: 'Consulta Médica',
    completado: 'Consulta Completada',
    error: 'Error en el Proceso'
};

export const METODOS_PAGO = {
    efectivo: '💵 Efectivo',
    tarjeta: '💳 Tarjeta de Crédito/Débito',
    transferencia: '🏦 Transferencia Bancaria'
};
