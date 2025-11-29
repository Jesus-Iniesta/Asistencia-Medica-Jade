// ========== CONFIGURACIÓN ==========
export const CONFIG = {
    API_URL: 'http://localhost:7070/api',
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

