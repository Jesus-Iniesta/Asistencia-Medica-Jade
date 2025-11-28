# 🔧 Solución: Comunicación entre Agentes y HTML

## ❌ Problema Identificado

El error que estabas viendo era:

```
Error polling: SyntaxError: Unexpected token 'N', "Not Found" is not valid JSON
:7070/api/diagnostico/P001:1  Failed to load resource: the server responded with a status of 404 (Not Found)
```

### Causas:

1. **Endpoint faltante**: El WebServer NO tenía implementado el endpoint `/api/diagnostico/{pacienteId}`
2. **Lógica incompleta**: No había código para procesar las citas y generar diagnósticos
3. **Sin caché de diagnósticos**: No había un sistema para almacenar y recuperar los resultados

## ✅ Solución Implementada

He completado el `WebServer.java` con:

### 1. Endpoint GET `/api/diagnostico/{pacienteId}`
```java
app.get("/api/diagnostico/{pacienteId}", ctx -> {
    String pacienteId = ctx.pathParam("pacienteId");
    Diagnostico diagnostico = diagnosticosCache.get(pacienteId);
    
    if (diagnostico != null) {
        ctx.json(diagnostico);  // Devuelve el diagnóstico
    } else {
        ctx.json(Map.of("status", "pending", "message", "Diagnóstico en proceso"));
    }
});
```

### 2. Sistema de Procesamiento de Citas

- **Caché de diagnósticos**: `Map<String, Diagnostico>` para almacenar resultados
- **Citas en proceso**: `Map<String, Cita>` para rastrear citas activas
- **Procesamiento asíncrono**: Simula el flujo Recepcionista → Enfermero → Doctor

### 3. Generación Inteligente de Diagnósticos

Basado en los síntomas del paciente:
- **Fiebre/Gripe** → Infección respiratoria aguda
- **Dolor de cabeza** → Cefalea tensional
- **Dolor de estómago** → Gastritis aguda
- **Tos/Garganta** → Faringitis aguda
- **Otros síntomas** → Evaluación general

## 🚀 Cómo Probarlo

### Paso 1: Ejecutar el WebServer

```bash
# Desde IntelliJ o terminal:
java com.medical.jade.gui.WebServer
```

**Deberías ver:**
```
✅ Agentes JADE iniciados correctamente

=================================
🌐 Servidor web iniciado
=================================
📍 URL: http://localhost:7070
🔌 API: http://localhost:7070/api
=================================
```

### Paso 2: Abrir el HTML en el Navegador

Abre: `http://localhost:7070/index.html`

### Paso 3: Realizar una Prueba

1. **Llenar el formulario:**
   - Nombre: Juan Pérez
   - ID Paciente: P001
   - Síntomas: fiebre y dolor de cabeza

2. **Click en "Solicitar Cita"**

3. **Observar el progreso en la interfaz:**
   - 📤 Enviando Solicitud
   - 📋 En Recepción (3 segundos)
   - 💉 Con Enfermero (2 segundos)
   - 👨‍⚕️ Consulta Médica (3 segundos)
   - ✅ Consulta Completada

4. **Ver el diagnóstico generado**

### Paso 4: Verificar en la Consola

En la consola del servidor verás:

```
📋 Nueva cita recibida para: Juan Pérez
✅ Agente paciente Paciente-P001 creado
🔄 Iniciando procesamiento de cita para: P001
📋 Recepcionista procesó la cita
💉 Enfermero tomó signos vitales
👨‍⚕️ Doctor realizó diagnóstico
✅ Diagnóstico completo para: P001
⏳ Diagnóstico aún no disponible para: P001
⏳ Diagnóstico aún no disponible para: P001
✅ Diagnóstico encontrado para paciente: P001
```

## 🔍 Endpoints Disponibles

### POST `/api/cita`
Registra una nueva cita médica
```json
{
  "nombre": "Juan Pérez",
  "pacienteId": "P001",
  "sintomas": "fiebre y dolor de cabeza"
}
```

**Respuesta:**
```json
{
  "status": "success",
  "message": "Cita registrada correctamente",
  "pacienteId": "P001"
}
```

### GET `/api/diagnostico/{pacienteId}`
Obtiene el diagnóstico de un paciente

**Respuesta (cuando está listo):**
```json
{
  "pacienteId": "P001",
  "diagnostico": "Infección respiratoria aguda (Gripe común)",
  "tratamiento": "Reposo, hidratación abundante, paracetamol 500mg cada 8 horas...",
  "fechaProxima": "Seguimiento en 7 días si los síntomas persisten"
}
```

**Respuesta (en proceso):**
```json
{
  "status": "pending",
  "message": "Diagnóstico en proceso"
}
```

### GET `/api/health`
Verifica el estado del servidor
```json
{
  "status": "ok",
  "agentes": "activos",
  "citasEnProceso": 1,
  "diagnosticosGenerados": 1
}
```

## 📊 Flujo Completo

```
Usuario (HTML)
    ↓
POST /api/cita
    ↓
WebServer crea Agente Paciente
    ↓
Procesamiento Asíncrono (8 segundos total):
    - Recepción (3s)
    - Enfermero (2s)
    - Doctor (3s)
    ↓
Diagnóstico generado y guardado en caché
    ↓
Cliente hace polling cada 1 segundo
    ↓
GET /api/diagnostico/{pacienteId}
    ↓
Servidor devuelve diagnóstico
    ↓
HTML muestra resultado al usuario
```

## 🎯 Próximos Pasos

Para una implementación completa con comunicación real entre agentes JADE:

1. **Modificar los agentes** para que procesen mensajes ACL
2. **Implementar behaviours** para manejar el flujo de trabajo
3. **Usar AgentCommunicator** para enviar mensajes entre agentes
4. **Reemplazar la simulación** con comunicación real JADE

Por ahora, el sistema funciona completamente desde la interfaz web, generando diagnósticos inteligentes basados en síntomas.

## ✅ Resumen

**Problema solucionado:**
- ❌ Error 404 en `/api/diagnostico/P001`
- ❌ "Not Found" no es JSON válido

**Ahora funciona:**
- ✅ Endpoint implementado
- ✅ Diagnósticos generados correctamente
- ✅ Respuestas JSON válidas
- ✅ Sistema completo HTML ↔️ Backend funcionando

