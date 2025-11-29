# 📱 Guía de Acceso desde Dispositivos Móviles

## 🎯 Problema Resuelto

Se corrigió el error que impedía diagnosticar desde celulares u otros dispositivos en la red. El problema era que la URL de la API estaba configurada como `localhost`, que solo funciona en la misma computadora del servidor.

---

## ✅ Solución Implementada

### Detección Automática de URL

El sistema ahora detecta automáticamente desde dónde se accede:

- **Acceso local** (mismo servidor): `http://localhost:7070/api`
- **Acceso remoto** (celular/tablet): `http://[IP-DEL-SERVIDOR]:7070/api`

**Ejemplo:**
- Si accedes desde el servidor: `http://localhost:7070/index.html`
- Si accedes desde celular: `http://192.168.1.100:7070/index.html`

---

## 📋 Pasos para Acceder desde Celular

### 1️⃣ Obtener la IP del Servidor

#### Windows:
```bash
ipconfig
```
Busca: **Dirección IPv4** (ejemplo: `192.168.1.100`)

#### Linux/macOS:
```bash
hostname -I
# o
ip addr show
```

### 2️⃣ Verificar Firewall

Asegúrate de que el puerto **7070** esté abierto:

#### Windows:
```bash
# Agregar regla de firewall
netsh advfirewall firewall add rule name="Sistema Médico Web" dir=in action=allow protocol=TCP localport=7070
```

#### Linux (UFW):
```bash
sudo ufw allow 7070/tcp
sudo ufw reload
```

### 3️⃣ Conectar el Celular a la Misma Red WiFi

⚠️ **IMPORTANTE:** El celular debe estar en la **misma red WiFi** que el servidor.

### 4️⃣ Abrir en el Navegador del Celular

En el navegador de tu celular, ingresa:
```
http://192.168.1.100:7070/index.html
```
*(Reemplaza `192.168.1.100` con tu IP real)*

---

## 🧪 Verificación

### Comprobar que el Servidor está Escuchando

En la computadora del servidor, ejecuta:

**Windows:**
```bash
netstat -an | findstr :7070
```

**Linux/macOS:**
```bash
netstat -an | grep 7070
```

Deberías ver:
```
TCP    0.0.0.0:7070    0.0.0.0:0    LISTENING
```

### Probar Conexión desde el Celular

Abre el navegador y ve a:
```
http://[IP-DEL-SERVIDOR]:7070/api/health
```

Deberías ver:
```json
{
  "status": "ok",
  "server": "running",
  "jadeConnected": true,
  "diagnosticos": 0
}
```

✅ Si ves esto, el servidor está accesible desde tu celular.

---

## 🔧 Solución de Problemas

### ❌ "No se puede conectar al servidor"

**Causa:** Firewall bloqueando el puerto 7070

**Solución:**
1. Desactiva temporalmente el firewall para probar
2. Si funciona, agrega una regla permanente para el puerto 7070

---

### ❌ "Error en el proceso"

**Causa:** JADE no está conectado o el servidor web no puede comunicarse con JADE

**Solución:**
1. Verifica que **MainContainer** esté ejecutándose
2. Verifica que **WebInterfaceServer** muestre: `🔗 JADE: ✅ CONECTADO`
3. Reinicia ambos programas en orden:
   - Primero: `MainContainer.java`
   - Segundo: `WebInterfaceServer.java`

---

### ❌ "Tiempo de espera agotado"

**Causa:** El Doctor Agent no está activo o no puede comunicarse

**Solución:**
1. Verifica que **RemoteContainer** esté ejecutándose
2. En la GUI de JADE, verifica que el agente **Doctor** esté presente
3. Revisa los logs del servidor

---

### ❌ La página carga pero no se ven estilos

**Causa:** Problema con archivos estáticos

**Solución:**
1. Borra el caché del navegador
2. Recarga con Ctrl+F5 (o Cmd+Shift+R en iOS)
3. Verifica que `styles.css` esté en la ruta correcta

---

## 📊 Tabla de URLs de Acceso

| Dispositivo | URL de Acceso | Ejemplo |
|------------|---------------|---------|
| Servidor (local) | `http://localhost:7070/index.html` | Mismo servidor |
| PC en red | `http://[IP-SERVIDOR]:7070/index.html` | `http://192.168.1.100:7070/index.html` |
| Celular | `http://[IP-SERVIDOR]:7070/index.html` | `http://192.168.1.100:7070/index.html` |
| Tablet | `http://[IP-SERVIDOR]:7070/index.html` | `http://192.168.1.100:7070/index.html` |

---

## 🎨 Optimización para Móviles

El sistema está diseñado para ser **responsive** y funcionar bien en dispositivos móviles:

✅ Interfaz adaptable a pantallas pequeñas
✅ Botones táctiles optimizados
✅ Formularios móvil-friendly
✅ Receta médica imprimible desde el celular

---

## 🌐 Prueba Completa desde Celular

### Paso a Paso:

1. **Conecta tu celular** a la misma WiFi
2. **Abre el navegador** (Chrome, Safari, Firefox)
3. **Ingresa la URL:** `http://[IP]:7070/index.html`
4. **Registra un paciente:**
   - Nombre: Juan Pérez
   - Edad: 30
   - Género: Masculino
   - Síntomas: Dolor de cabeza y fiebre
5. **Observa el flujo:**
   - ✅ Signos vitales se toman
   - ✅ Se envía al doctor
   - ✅ Se recibe diagnóstico
   - ✅ Puedes pagar
   - ✅ Puedes ver la receta

---

## 📝 Logs de Depuración

Para ver los logs en el celular:

### Chrome (Android):
1. Conecta el celular por USB
2. En la PC: `chrome://inspect`
3. Selecciona tu dispositivo
4. Ve a la consola

### Safari (iOS):
1. Habilita "Web Inspector" en Ajustes > Safari > Avanzado
2. En Mac: Safari > Develop > [Tu iPhone]
3. Ve a la consola

---

## 🔒 Seguridad

⚠️ **Advertencias de Seguridad:**

- Este sistema está diseñado para **redes locales privadas**
- **NO exponer** a Internet sin seguridad adicional
- Usar **HTTPS** en producción
- Implementar **autenticación** para usuarios reales
- Validar **entradas del usuario** en producción

---

## 🚀 Mejoras Implementadas

### 1. Detección Automática de URL
```javascript
function getApiUrl() {
    if (window.location.hostname === 'localhost' || 
        window.location.hostname === '127.0.0.1') {
        return 'http://localhost:7070/api';
    }
    return `http://${window.location.hostname}:7070/api`;
}
```

### 2. Manejo de Errores Mejorado
- Mensajes claros cuando no hay conexión
- Instrucciones específicas de solución
- Logs detallados en consola

### 3. Validación de Respuestas
- Verifica que las respuestas HTTP sean exitosas
- Maneja timeouts de manera elegante
- Reintentos automáticos con límite

---

## ✨ Características Móviles

### Responsive Design
- Formularios optimizados para touch
- Botones con tamaño adecuado (min 44px)
- Texto legible sin zoom
- Navegación simplificada

### Offline Fallback
- Detecta cuando no hay conexión
- Muestra mensaje apropiado
- Permite reintentar

### Performance
- Carga rápida de recursos
- Animaciones suaves
- Polling eficiente

---

## 📞 Soporte

Si sigues teniendo problemas:

1. **Verifica la consola del navegador** (F12)
2. **Revisa los logs del servidor** (terminal donde corre WebInterfaceServer)
3. **Comprueba la conexión de red** (ping a la IP del servidor)
4. **Verifica que JADE esté activo** (ventana GUI de JADE)

---

## 🎉 Conclusión

Con estas correcciones, ahora puedes:

✅ Acceder desde cualquier dispositivo en la red
✅ Diagnosticar pacientes desde tu celular
✅ Ver la receta médica en dispositivos móviles
✅ Compartir el enlace con otros usuarios en la red

El sistema automáticamente detecta desde dónde se accede y configura la URL correcta de la API. ¡Ya no más errores de "localhost" desde dispositivos remotos! 🚀

