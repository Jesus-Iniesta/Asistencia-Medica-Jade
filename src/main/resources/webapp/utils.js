// ========== UTILIDADES ==========
export function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

export function generarIDUnico() {
    const timestamp = Date.now();
    const random = Math.floor(Math.random() * 1000);
    return `P${timestamp}${random}`;
}

export function generarFolio() {
    return 'F' + Date.now().toString().slice(-8);
}

export function generarSignosVitales(config) {
    return {
        temperatura: (config.TEMPERATURA.min + Math.random() * (config.TEMPERATURA.max - config.TEMPERATURA.min)).toFixed(1),
        altura: (config.ALTURA.min + Math.random() * (config.ALTURA.max - config.ALTURA.min)).toFixed(0),
        ritmo: (config.RITMO.min + Math.random() * (config.RITMO.max - config.RITMO.min)).toFixed(0),
        presion: `${(config.PRESION_SYS.min + Math.random() * (config.PRESION_SYS.max - config.PRESION_SYS.min)).toFixed(0)}/${(config.PRESION_DIA.min + Math.random() * (config.PRESION_DIA.max - config.PRESION_DIA.min)).toFixed(0)}`
    };
}

export function getFechaActual() {
    return new Date().toLocaleDateString('es-MX', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

export function getHoraActual() {
    return new Date().toLocaleTimeString('es-MX', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function getFechaHoraCompleta() {
    return new Date().toLocaleString('es-MX', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function determinarUrgencia(diagnostico) {
    if (diagnostico.includes('URGENTE') || diagnostico.includes('🚨')) {
        return { nivel: 'urgente', icono: '🚨', color: '#ef4444' };
    } else if (diagnostico.includes('Requiere atención') || diagnostico.includes('⚠️')) {
        return { nivel: 'importante', icono: '⚠️', color: '#f59e0b' };
    }
    return { nivel: 'normal', icono: '✅', color: '#10b981' };
}

export function determinarCategoria(diagnostico) {
    const categorias = [
        { keywords: ['respiratori', 'Tos', 'Faringitis', 'Bronquitis'], nombre: 'Respiratorio', icono: '🫁' },
        { keywords: ['Gastro', 'Diarrea', 'estómago', 'náuseas', 'Dispepsia', 'Estreñimiento'], nombre: 'Gastrointestinal', icono: '🫃' },
        { keywords: ['Cefalea', 'Migraña'], nombre: 'Neurológico', icono: '🧠' },
        { keywords: ['cardio', 'presión', 'pecho'], nombre: 'Cardiovascular', icono: '❤️' }
    ];

    for (const cat of categorias) {
        if (cat.keywords.some(kw => diagnostico.includes(kw))) {
            return { nombre: cat.nombre, icono: cat.icono };
        }
    }

    return { nombre: 'General', icono: '🩺' };
}

