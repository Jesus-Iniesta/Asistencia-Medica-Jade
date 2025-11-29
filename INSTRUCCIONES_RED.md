# 🌐 Instrucciones para Usar el Sistema en Red con Dos Computadoras

## 📋 Índice
1. [Requisitos Previos](#requisitos-previos)
2. [Configuración de Red](#configuración-de-red)
3. [Computadora Principal](#computadora-principal-servidor)
4. [Computadora Secundaria](#computadora-secundaria-cliente)
5. [Prueba del Sistema](#prueba-del-sistema)
6. [Solución de Problemas](#solución-de-problemas)

---

## ✅ Requisitos Previos

### Hardware
- 🖥️ **Computadora Principal (Servidor):**
  - Mínimo 4GB RAM
  - Java 17 o superior
  - Sistema operativo: Windows, Linux o macOS

- 💻 **Computadora Secundaria (Cliente):**
  - Mínimo 2GB RAM
  - Java 17 o superior
  - Sistema operativo: Windows, Linux o macOS

### Software
- ☕ **Java JDK 17+** instalado en ambas computadoras
- 📦 **Maven** (incluido en el proyecto)
- 🌐 **Conexión de red** entre ambas computadoras

### Red
- 📡 Ambas computadoras en la **misma red local** (WiFi o Ethernet)
- 🔓 **Puerto 1099** abierto en el firewall (JADE)
- 🔓 **Puerto 7070** abierto en el firewall (Servidor Web)

---

## 🔧 Configuración de Red

### Paso 1: Identificar la IP de la Computadora Principal

#### En Windows:
```bash
ipconfig
```
Busca la línea que dice **"Dirección IPv4"**, por ejemplo: `192.168.1.100`

#### En Linux/macOS:
```bash
ifconfig
# o
ip addr show
```
Busca la dirección IP en la interfaz activa (eth0, wlan0, etc.)

**Ejemplo de salida:**
```
Dirección IPv4: 192.168.1.100
Máscara de subred: 255.255.255.0
Puerta de enlace: 192.168.1.1
```

⚠️ **IMPORTANTE:** Anota esta IP, la necesitarás para configurar la computadora secundaria.

---

### Paso 2: Configurar Firewall

#### Windows (Firewall de Windows Defender):

1. Abre **"Panel de Control" → "Sistema y seguridad" → "Firewall de Windows Defender"**
2. Clic en **"Configuración avanzada"**
3. Selecciona **"Reglas de entrada"**
4. Clic en **"Nueva regla"**
5. Selecciona **"Puerto"** → Siguiente
6. Selecciona **"TCP"** y escribe: `1099, 7070`
7. Selecciona **"Permitir la conexión"**
8. Marca todas las opciones (Dominio, Privado, Público)
9. Dale un nombre: **"JADE Sistema Médico"**

#### Linux (UFW):
```bash
sudo ufw allow 1099/tcp
sudo ufw allow 7070/tcp
sudo ufw reload
```

#### macOS:
```bash
# Ir a Preferencias del Sistema → Seguridad y Privacidad → Firewall
# Clic en "Opciones del Firewall"
# Permitir conexiones entrantes para Java
```

---

## 🖥️ Computadora Principal (Servidor)

### Paso 1: Compilar el Proyecto

```bash
cd /ruta/al/proyecto/SistemaMedico
mvn clean package
```

Deberías ver:
```
[INFO] BUILD SUCCESS
```

---

### Paso 2: Iniciar MainContainer

```bash
java -cp target/classes:lib/* com.medical.jade.launcher.MainContainer
```

**Salida esperada:**
```
===========================================
🏥 SISTEMA MÉDICO - PLATAFORMA JADE
===========================================
✅ Plataforma JADE iniciada
📍 Host: localhost
📡 Puerto: 1099
===========================================

👨‍💼 Recepcionista iniciado: Recepcionista
💉 Enfermero iniciado: Enfermero

✅ Servicios registrados en Yellow Pages
===========================================
```

✅ **Verás una ventana gráfica de JADE con los agentes Recepcionista y Enfermero**

⚠️ **Mantén esta ventana abierta**

---

### Paso 3: Iniciar WebInterfaceServer

Abre una **nueva terminal** y ejecuta:

```bash
java -cp target/classes:lib/* com.medical.jade.launcher.WebInterfaceServer
```

**Salida esperada:**
```
===========================================
🔄 INICIANDO SERVIDOR WEB...
===========================================
🔌 Intentando conectar a plataforma JADE...
✅ Conectado a plataforma JADE exitosamente
📦 Contenedor: web-container

===========================================
🌐 INTERFAZ WEB - INICIADA
===========================================
📍 URL: http://localhost:7070
📄 Interfaz: http://localhost:7070/index.html
🔌 API: http://localhost:7070/api
🔗 JADE: ✅ CONECTADO
===========================================

💡 INSTRUCCIONES:
   1. Abre tu navegador
   2. Ve a: http://localhost:7070/index.html
   3. Llena el formulario de cita médica
   4. Observa la comunicación entre agentes
===========================================
```

✅ **El servidor web está listo**

⚠️ **Mantén esta ventana abierta**

---

### Paso 4: Verificar Conexión Web

Abre tu navegador en: **http://localhost:7070/index.html**

Deberías ver la interfaz del sistema médico.

---

## 💻 Computadora Secundaria (Cliente)

### Paso 1: Copiar el Proyecto

Opciones:
- **Git:** `git clone [url-repositorio]`
- **USB:** Copiar toda la carpeta del proyecto
- **Red:** Compartir carpeta desde la principal

---

### Paso 2: Configurar la IP del Servidor

Abre el archivo: `src/main/java/com/medical/jade/launcher/RemoteContainer.java`

**Busca estas líneas (aproximadamente línea 24):**

```java
// OPCIÓN A: Prueba en la MISMA computadora (desarrollo)
String mainHost = "localhost";

// OPCIÓN B: Otra computadora en la red
// Descomentar y cambiar XXX por la IP real de la computadora principal
// Ejemplo: String mainHost = "192.168.1.100";
// String mainHost = "192.168.1.XXX";
```

**Modifica para usar la IP de tu computadora principal:**

```java
// OPCIÓN A: Prueba en la MISMA computadora (desarrollo)
// String mainHost = "localhost";  // ← COMENTAR ESTA LÍNEA

// OPCIÓN B: Otra computadora en la red
// Descomentar y cambiar XXX por la IP real de la computadora principal
String mainHost = "192.168.1.100";  // ← USAR TU IP AQUÍ
```

⚠️ **IMPORTANTE:** Reemplaza `192.168.1.100` con la IP que anotaste antes.

---

### Paso 3: Compilar el Proyecto

```bash
cd /ruta/al/proyecto/SistemaMedico
mvn clean package
```

---

### Paso 4: Iniciar RemoteContainer

```bash
java -cp target/classes:lib/* com.medical.jade.launcher.RemoteContainer
```

**Salida esperada:**
```
===========================================
🔄 INICIANDO CONTENEDOR REMOTO...
===========================================
🔌 Conectando a: 192.168.1.100:1099
✅ Conectado exitosamente
===========================================

👨‍⚕️ Creando agente Doctor...

===========================================
✅ CONTENEDOR REMOTO ACTIVO
===========================================
📍 Host principal: 192.168.1.100
👨‍⚕️ Agente activo: Doctor
===========================================

💡 El Doctor está listo para atender pacientes
🔗 Comunicándose con la computadora principal

⚠️  Mantén esta ventana abierta para que el Doctor siga activo
```

✅ **El Doctor se ha conectado exitosamente**

---

### Verificación en la Computadora Principal

En la **ventana de JADE** de la computadora principal, deberías ver:

```
📦 Nuevo contenedor conectado: remote-container
👨‍⚕️ Nuevo agente: Doctor@remote-container
```

En la GUI de JADE, verás el agente **Doctor** en el contenedor **remote-container**.

---

## 🧪 Prueba del Sistema

### Flujo de Prueba Completo

#### 1. En la Computadora Principal:

Abre el navegador en: **http://localhost:7070/index.html**

#### 2. Registra un Paciente:

Llena el formulario:
- **Nombre:** Juan Pérez
- **Edad:** 35
- **Género:** Masculino
- **Síntomas:** Dolor de cabeza y fiebre

Clic en **"Registrar y Continuar"**

#### 3. Observa el Flujo:

**En la terminal del MainContainer:**
```
📋 Cita recibida desde web
👨‍💼 Recepcionista: Procesando solicitud de cita
💉 Enfermero: Tomando signos vitales
📤 Enviando caso médico al Doctor...
```

**En la terminal del RemoteContainer (Computadora Secundaria):**
```
👨‍⚕️ Doctor: Caso médico recibido
🔍 Analizando síntomas: Dolor de cabeza y fiebre
📊 Diagnóstico generado: Cefalea con síndrome febril
💊 Tratamiento: Paracetamol 500mg cada 8 horas
✅ Diagnóstico enviado
```

**En la terminal del WebServer:**
```
✅ Diagnóstico guardado para: P1732836000123
```

#### 4. Ver Diagnóstico:

En el navegador, verás automáticamente:
- ✅ Diagnóstico médico
- 💊 Tratamiento prescrito
- 📅 Próxima cita

#### 5. Procesar Pago:

Clic en **"Proceder al Pago"**
Selecciona un método de pago
Verás el recibo con folio

#### 6. Ver Receta Médica:

Clic en **"Ver Receta Médica"**
Tendrás una receta completa con todos los datos

---

## 📊 Diagrama de Arquitectura en Red

```
┌─────────────────────────────────────────────────────────────┐
│                   COMPUTADORA PRINCIPAL                     │
│                     (192.168.1.100)                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────┐      ┌──────────────────────┐   │
│  │   MainContainer      │      │   WebContainer       │   │
│  │   Puerto: 1099       │◄─────┤   Puerto: 7070       │   │
│  │                      │      │                      │   │
│  │  👨‍💼 Recepcionista    │      │  🌐 Servidor Web     │   │
│  │  💉 Enfermero        │      │  👤 Pacientes        │   │
│  │                      │      │     (dinámicos)      │   │
│  └──────────────────────┘      └──────────────────────┘   │
│           ▲                              ▲                 │
│           │                              │                 │
└───────────┼──────────────────────────────┼─────────────────┘
            │                              │
            │ JADE Messages                │ HTTP
            │ (Puerto 1099)                │ (Puerto 7070)
            │                              │
┌───────────┼──────────────────────────────┼─────────────────┐
│           ▼                              │                 │
│  ┌──────────────────────┐                │                 │
│  │  RemoteContainer     │                │                 │
│  │                      │                │                 │
│  │  👨‍⚕️ Doctor           │                │                 │
│  │                      │                │                 │
│  └──────────────────────┘                │                 │
│                                          ▼                 │
│                    COMPUTADORA SECUNDARIA                  │
│                      (192.168.1.XXX)                       │
│                                                             │
│              [Usuario accede desde navegador]              │
│          http://192.168.1.100:7070/index.html              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔍 Solución de Problemas

### ❌ Error: "No se pudo conectar a JADE"

**Síntomas:**
```
❌ ERROR AL CONECTAR
```

**Soluciones:**

1. **Verificar que MainContainer esté ejecutándose:**
   ```bash
   # En la computadora principal, debería haber una ventana de JADE abierta
   ```

2. **Verificar la IP:**
   ```bash
   # Asegúrate de que la IP en RemoteContainer.java sea correcta
   ping 192.168.1.100  # Desde la computadora secundaria
   ```

3. **Verificar firewall:**
   ```bash
   # Windows
   netstat -an | findstr 1099
   
   # Linux/Mac
   netstat -an | grep 1099
   ```
   Deberías ver: `0.0.0.0:1099` o `*:1099`

4. **Probar conexión:**
   ```bash
   telnet 192.168.1.100 1099
   ```
   Si conecta, el puerto está abierto.

---

### ❌ Error: "JADE no está conectado" (en el navegador)

**Síntomas:**
```json
{
  "status": "error",
  "message": "JADE no está conectado..."
}
```

**Soluciones:**

1. **Reiniciar en orden correcto:**
   ```bash
   # Detener todo (Ctrl+C en todas las terminales)
   
   # 1. MainContainer
   java -cp target/classes:lib/* com.medical.jade.launcher.MainContainer
   
   # 2. WebInterfaceServer (esperar que JADE esté listo)
   java -cp target/classes:lib/* com.medical.jade.launcher.WebInterfaceServer
   
   # 3. RemoteContainer
   java -cp target/classes:lib/* com.medical.jade.launcher.RemoteContainer
   ```

2. **Verificar logs:**
   En la terminal del WebInterfaceServer debería decir:
   ```
   🔗 JADE: ✅ CONECTADO
   ```

---

### ❌ Error: "Cannot reach the remote container"

**Síntomas:**
La computadora secundaria no puede conectarse a la principal.

**Soluciones:**

1. **Verificar red:**
   ```bash
   ping 192.168.1.100
   ```

2. **Desactivar firewall temporalmente** (solo para prueba):
   ```bash
   # Windows
   netsh advfirewall set allprofiles state off
   
   # Linux
   sudo ufw disable
   ```

3. **Usar IP estática:**
   Configura una IP fija en la computadora principal para evitar cambios.

---

### ❌ No aparece el Doctor en JADE GUI

**Soluciones:**

1. **Refrescar JADE GUI:**
   - Clic derecho en el árbol de agentes
   - Selecciona "Refresh"

2. **Verificar logs del RemoteContainer:**
   Debe decir: `✅ CONTENEDOR REMOTO ACTIVO`

3. **Reiniciar RemoteContainer:**
   Presiona `Ctrl+C` y vuelve a ejecutar.

---

### ❌ "Address already in use" (Puerto ocupado)

**Síntomas:**
```
java.net.BindException: Address already in use
```

**Soluciones:**

1. **Buscar proceso usando el puerto:**
   ```bash
   # Windows
   netstat -ano | findstr :7070
   taskkill /PID [número] /F
   
   # Linux/Mac
   lsof -i :7070
   kill -9 [PID]
   ```

2. **Esperar un momento:**
   A veces el puerto tarda en liberarse (30 segundos).

---

## 📱 Acceso desde Otras Computadoras

Cualquier dispositivo en la red puede acceder a la interfaz web:

```
http://192.168.1.100:7070/index.html
```

Esto permite:
- 📱 Teléfonos móviles
- 💻 Laptops adicionales
- 🖥️ Otras computadoras de escritorio

**Ejemplo:**
```
Computadora Principal: 192.168.1.100
   - MainContainer
   - WebInterfaceServer

Computadora 2: 192.168.1.101
   - RemoteContainer (Doctor)

Computadora 3: 192.168.1.102
   - Solo navegador web

Tablet: 192.168.1.103
   - Solo navegador web
```

---

## 📋 Checklist de Verificación

Antes de reportar un problema, verifica:

- [ ] Ambas computadoras tienen Java 17+
- [ ] Ambas computadoras están en la misma red
- [ ] La IP de la computadora principal es correcta
- [ ] El firewall permite los puertos 1099 y 7070
- [ ] MainContainer se inició primero
- [ ] WebInterfaceServer dice "JADE: ✅ CONECTADO"
- [ ] RemoteContainer se conectó exitosamente
- [ ] La GUI de JADE muestra todos los agentes

---

## 🎯 Resumen Rápido

### Computadora Principal (Servidor):
```bash
# Terminal 1
java -cp target/classes:lib/* com.medical.jade.launcher.MainContainer

# Terminal 2
java -cp target/classes:lib/* com.medical.jade.launcher.WebInterfaceServer
```

### Computadora Secundaria (Cliente):
```bash
# Editar RemoteContainer.java con la IP correcta
# Compilar: mvn clean package

# Terminal 1
java -cp target/classes:lib/* com.medical.jade.launcher.RemoteContainer
```

### Navegador (Cualquier dispositivo):
```
http://192.168.1.100:7070/index.html
```

---

## 📞 Soporte Adicional

Si sigues teniendo problemas:

1. Revisa los logs completos de cada terminal
2. Verifica la conectividad de red con `ping`
3. Prueba primero en `localhost` antes de red
4. Consulta la documentación de JADE: https://jade.tilab.com/

---

¡Sistema listo para funcionar en red distribuida! 🎉

