# 🚀 Guía Rápida - Ver Interfaz HTML

## 📋 Orden de Ejecución (3 pasos)

### **PASO 1**: Iniciar Contenedor Principal
```
Ejecuta: MainContainer.java
```
Verás:
```
🏥 COMPUTADORA PRINCIPAL - INICIADA
✅ AGENTES EN COMPUTADORA PRINCIPAL:
   1. Recepcionista
   2. Enfermero
```

### **PASO 2**: Iniciar Contenedor de Prueba
```
Ejecuta: LocalTestContainer.java
```
Verás:
```
🧪 CONTENEDOR DE PRUEBA LOCAL - INICIADO
✅ AGENTES EN CONTENEDOR DE PRUEBA:
   1. Doctor
   2. Paciente-P001
```

### **PASO 3**: Iniciar Interfaz Web
```
Ejecuta: WebInterfaceServer.java
```
Verás:
```
🌐 INTERFAZ WEB - INICIADA
📍 URL: http://localhost:7070
```

---

## 🌐 Acceder a la Interfaz

Una vez que ejecutes los 3 pasos, abre tu navegador en:

### 🔗 http://localhost:7070/index.html

o simplemente:

### 🔗 http://localhost:7070

---

## 📝 Usar la Interfaz

1. **Llena el formulario**:
   - Nombre: Ej. "Juan Pérez"
   - ID Paciente: Ej. "P002"
   - Síntomas: Ej. "Fiebre y tos"

2. **Haz clic en "Solicitar Cita"**

3. **Observa**:
   - Los agentes JADE procesarán la cita
   - Verás mensajes en las consolas de MainContainer y LocalTestContainer
   - El diagnóstico aparecerá en la interfaz web

---

## 🔧 Verificación Rápida

### ¿Todo funcionando?
- ✅ MainContainer ejecutándose
- ✅ LocalTestContainer ejecutándose
- ✅ WebInterfaceServer ejecutándose
- ✅ Navegador en http://localhost:7070

### API Endpoints disponibles:
- `GET /api/health` - Estado del servidor
- `POST /api/cita` - Enviar nueva cita
- `GET /api/diagnostico/{pacienteId}` - Obtener diagnóstico

---

## 🎯 Resumen de Ejecución

```
Terminal 1:  MainContainer.java          → Puerto 1099 (JADE)
Terminal 2:  LocalTestContainer.java     → Se conecta al puerto 1099
Terminal 3:  WebInterfaceServer.java     → Puerto 7070 (HTTP)
Navegador:   http://localhost:7070       → Interfaz web
```

---

## ⚠️ Solución de Problemas

### No carga la página
- Verifica que WebInterfaceServer esté ejecutándose
- Confirma que no haya otro servicio en el puerto 7070

### Error 404 en recursos
- Los archivos HTML están en: `src/main/resources/webapp/`
- Asegúrate de haber compilado con `mvn compile`

### Los agentes no responden
- Verifica que MainContainer y LocalTestContainer estén ejecutándose
- Revisa las consolas para ver los mensajes de los agentes

