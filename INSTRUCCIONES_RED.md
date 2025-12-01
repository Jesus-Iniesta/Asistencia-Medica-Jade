# 🌐 Instrucciones para Usar el Sistema en Red con Dos Computadoras

## 📋 Índice
1. [Requisitos Previos](#requisitos-previos)
2. [Configuración de Red](#configuración-de-red)
3. [Computadora Principal](#computadora-principal-servidor)
4. [Computadora Secundaria](#computadora-secundaria-cliente)
5. [Prueba del Sistema](#prueba-del-sistema)
6. [Solución de Problemas](#solución-de-problemas)
7. [Puente TCP (NetworkBridgeAgent)](#puente-tcp-networkbridgeagent)

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
- 🔓 **Puerto 6200** (o el definido en `-Dbridge.port`) abierto para el **NetworkBridgeAgent**

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
ip addr show
# o
ifconfig
```
Busca la dirección IP en la interfaz activa (wlan0, eth0, enp3s0, etc.)

**Ejemplo de salida:**
```
3: wlan0: <BROADCAST,MULTICAST,UP,LOWER_UP>
    inet 192.168.1.100/24 brd 192.168.1.255 scope global dynamic
```

💡 **TIP CRÍTICO:** Si `MainContainer` imprime una IP diferente (ej. `192.168.56.x` de VirtualBox), ignora ese valor y usa la IP real que obtuviste con `ipconfig/ifconfig`. También puedes forzarla al iniciar con `-Dmain.host=172.22.112.1`.

---

### Paso 2: Configurar Firewall (MUY IMPORTANTE)

El error **"No existe ninguna ruta hasta el host"** generalmente se debe al firewall bloqueando las conexiones.

#### Windows (Firewall de Windows Defender):

**Opción 1: Permitir Java en el Firewall (Recomendado)**
1. Abre **"Panel de Control" → "Sistema y seguridad" → "Firewall de Windows Defender"**
2. Clic en **"Permitir una aplicación o una característica a través de Firewall de Windows Defender"**
3. Clic en **"Cambiar configuración"**
4. Clic en **"Permitir otra aplicación"**
5. Busca y selecciona **`java.exe`** y **`javaw.exe`** (normalmente en `C:\Program Files\Java\jdk-17\bin\`)
6. Marca todas las casillas (Privado y Público)
7. Clic en **"Agregar"**

**Opción 2: Crear Regla de Puerto**
1. Abre **"Panel de Control" → "Sistema y seguridad" → "Firewall de Windows Defender"**
2. Clic en **"Configuración avanzada"**
3. Selecciona **"Reglas de entrada"**
4. Clic en **"Nueva regla"**
5. Selecciona **"Puerto"** → Siguiente
6. Selecciona **"TCP"** y escribe: `1099, 7070, 6200`
7. Selecciona **"Permitir la conexión"**
8. Marca todas las opciones (Dominio, Privado, Público)
9. Dale un nombre: **"JADE Sistema Médico"**

#### Linux (UFW):
```bash
# Permitir puertos
sudo ufw allow 1099/tcp
sudo ufw allow 7070/tcp
sudo ufw allow 6200/tcp
sudo ufw reload

# Verificar reglas
sudo ufw status
```

#### Linux (Fedora/CentOS/RHEL):
```bash
# Permitir puertos
sudo firewall-cmd --permanent --add-port=1099/tcp
sudo firewall-cmd --permanent --add-port=7070/tcp
sudo firewall-cmd --permanent --add-port=6200/tcp
sudo firewall-cmd --reload

# Verificar
sudo firewall-cmd --list-ports
```

#### macOS:
```bash
# Ir a Preferencias del Sistema → Seguridad y Privacidad → Firewall
# Clic en "Opciones del Firewall"
# Permitir conexiones entrantes para Java
```

---

### Paso 3: Verificar Conectividad entre Computadoras

Antes de continuar, **verifica que ambas computadoras pueden comunicarse**:

#### Desde la Computadora Secundaria, ejecuta:

```bash
# Verificar que puedes hacer ping a la principal
ping 192.168.1.100

# Deberías ver:
# 64 bytes from 192.168.1.100: icmp_seq=1 ttl=64 time=2.5 ms
```

Si el ping **NO funciona**:
- ❌ Verifica que ambas estén en la misma red WiFi
- ❌ Desactiva temporalmente el firewall para probar
- ❌ Verifica que no haya aislamiento de clientes en el router

Si el ping **SÍ funciona**, prueba la conectividad del puerto:

```bash
# Linux/macOS
telnet 192.168.1.100 1099

# Windows (PowerShell)
Test-NetConnection -ComputerName 192.168.1.100 -Port 1099
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

```powershell
# Windows PowerShell
dotnet ;
```

```bash
# Linux/macOS
dotnet ;
```

```bash
# Ejecución estándar
java -cp target/classes com.medical.jade.launcher.MainContainer

# Si deseas forzar la IP detectada (recomendado cuando hay adaptadores virtuales)
java -Dmain.host=172.22.112.1 -cp target/classes com.medical.jade.launcher.MainContainer
```

**Flags opcionales:**
- `-Dbridge.port=6300` → cambia el puerto del socket puente si 6200 ya está ocupado.

**Salida esperada:**
```
===========================================
🔍 DETECTANDO CONFIGURACIÓN DE RED...
===========================================
📍 IP detectada: 192.168.1.100

===========================================
🏥 COMPUTADORA PRINCIPAL - INICIADA
===========================================
📍 IP del Servidor: 192.168.1.100
🔌 Puerto JADE: 1099
🌐 Puerto Web: 7070
🔗 Puerto Socket JADE Bridge: 6200
===========================================

📋 INSTRUCCIONES PARA COMPUTADORA SECUNDARIA:
   1. Abre RemoteContainer.java
   2. Cambia la línea 26 a:
      String mainHost = "192.168.1.100";
   3. Ajusta RemoteContainer para usar el puerto 6200 (bridgePort)
   4. Ejecuta RemoteContainer
===========================================

✅ AGENTES ACTIVOS EN COMPUTADORA PRINCIPAL:
   1. Recepcionista - Registra citas
   2. Enfermero - Toma signos vitales

⏳ Esperando conexión de Computadora Secundaria (Doctor)...
```

⚠️ **COPIA LA IP QUE MUESTRA** (en este ejemplo: `192.168.1.100`)

✅ **Verás una ventana gráfica de JADE con los agentes**

⚠️ **Mantén esta ventana abierta**

---

### Paso 3: Iniciar WebInterfaceServer

Abre una **nueva terminal** y ejecuta:

```bash
java -cp target/classes com.medical.jade.launcher.WebInterfaceServer
```

**Salida esperada:**
```
===========================================
🌐 INTERFAZ WEB - INICIADA
===========================================
📍 URL: http://192.168.1.100:7070
📄 Interfaz: http://192.168.1.100:7070/index.html
🔌 API: http://192.168.1.100:7070/api
🔗 JADE: ✅ CONECTADO
===========================================
```

✅ **El servidor web está listo**

⚠️ **Mantén esta ventana abierta**

---

## 💻 Computadora Secundaria (Cliente)

### Paso 1: Copiar el Proyecto

Opciones:
- **Git:** `git clone [url-repositorio]`
- **USB:** Copiar toda la carpeta del proyecto
- **Red:** Compartir carpeta desde la principal

---

### Paso 2: Configurar la IP del Servidor

1. Abre `RemoteContainer.java`
2. Reemplaza `mainHost` con la IP detectada por el servidor.
3. (Opcional) Si cambiaste el puerto del puente, agrega `-Dbridge.port=PUERTO` al ejecutar este contenedor o ajusta la propiedad `bridgePort` en Java.

### Paso 4: Ejecutar RemoteContainer

```bash
java -cp target/classes com.medical.jade.launcher.RemoteContainer
```

**Salida esperada:**
```
===========================================
🔄 INICIANDO CONTENEDOR REMOTO...
===========================================
🔌 Intentando conectar a: 192.168.1.100:1099
⏳ Esto puede tomar unos segundos...

✅ Conexión establecida con el MainContainer
===========================================

👨‍⚕️ Creando agente Doctor...

===========================================
✅ CONTENEDOR REMOTO ACTIVO
===========================================
📍 Conectado a: 192.168.1.100
👨‍⚕️ Agente activo: Doctor
🔗 Bridge TCP activo en puerto remoto: 6200
===========================================

💡 El Doctor está listo para atender pacientes
🔗 Comunicándose con la computadora principal

⚠️  Mantén esta ventana abierta para que el Doctor siga activo
```

✅ **¡Conexión exitosa!** Ahora verás el agente **Doctor** en la ventana de JADE de la computadora principal.

---

## 🧪 Prueba del Sistema

### Desde cualquier dispositivo en la misma red:

1. Abre un navegador
2. Ve a: **http://192.168.1.100:7070/index.html** (usa la IP de la computadora principal)
3. Llena el formulario de cita médica
4. Observa cómo los agentes se comunican entre las dos computadoras

---

## 🔧 Solución de Problemas

### ❌ Error: "No existe ninguna ruta hasta el host"

**Causas comunes:**
1. **Firewall bloqueando conexiones**
2. **Computadoras en redes diferentes**
3. **IP incorrecta**
4. **VirtualBox/Docker interferiendo**

**Soluciones:**

#### 1. Verificar Firewall (Más común)

**Windows:**
```powershell
# Desactivar temporalmente para probar
netsh advfirewall set allprofiles state off

# Si funciona, el problema es el firewall
# Vuelve a activarlo:
netsh advfirewall set allprofiles state on

# Y agrega las reglas como se explicó arriba
```

**Linux:**
```bash
# Verificar estado del firewall
sudo ufw status

# Desactivar temporalmente para probar
sudo ufw disable

# Si funciona, vuelve a activar y agrega reglas
sudo ufw enable
sudo ufw allow 1099/tcp
sudo ufw allow 7070/tcp
sudo ufw allow 6200/tcp
```

#### 2. Verificar que están en la misma red

Ambas computadoras deben tener IPs en el mismo rango:
- ✅ Computadora 1: `192.168.1.100`
- ✅ Computadora 2: `192.168.1.101`
- ❌ Computadora 1: `192.168.1.100`
- ❌ Computadora 2: `10.0.0.5` (red diferente)

#### 3. Verificar conectividad básica

```bash
# Desde la computadora secundaria
ping 192.168.1.100

# Si el ping falla:
# - Verifica la IP con ipconfig/ifconfig
# - Conecta ambas a la misma red WiFi
# - Desactiva "Aislamiento de cliente" en el router
```

#### 4. VirtualBox/Docker interferiendo

Si tienes VirtualBox o Docker, pueden crear interfaces de red que interfieren:

```bash
# Linux: Ver todas las interfaces
ip addr show

# Desactivar interfaces virtuales temporalmente
sudo ifconfig vboxnet0 down
sudo ifconfig docker0 down
```

El código actualizado en **MainContainer.java** ya filtra automáticamente estas interfaces.

---

### ❌ Error: "Connection refused"

**Causa:** MainContainer no está ejecutándose.

**Solución:** Asegúrate de que MainContainer esté corriendo en la computadora principal ANTES de ejecutar RemoteContainer.

---

### ❌ Error: Agente Doctor no aparece en JADE GUI

**Causa:** RemoteContainer no se conectó correctamente.

**Solución:**
1. Revisa la salida de RemoteContainer
2. Verifica que diga "✅ Conexión establecida"
3. En la GUI de JADE, ve a **Tools → Remote Agent Management**
4. Deberías ver el contenedor "remote-container"

---

### 🔍 Diagnóstico Avanzado

Si nada funciona, ejecuta estos comandos:

**En la Computadora Principal:**
```bash
# Verificar que Java está escuchando en el puerto 1099
netstat -an | grep 1099

# Deberías ver algo como:
# tcp        0      0 0.0.0.0:1099            0.0.0.0:*               LISTEN
```

**En la Computadora Secundaria:**
```bash
# Verificar conectividad al puerto
telnet 192.168.1.100 1099

# Si se conecta, verás:
# Trying 192.168.1.100...
# Connected to 192.168.1.100.
```

---

## 📱 Acceso desde Dispositivos Móviles

Una vez que el sistema esté funcionando, puedes acceder desde tu celular o tablet:

1. Conecta tu dispositivo móvil a la **misma red WiFi**
2. Abre el navegador móvil
3. Ve a: **http://192.168.1.100:7070/index.html**
4. ¡Listo! Puedes usar el sistema desde tu celular

---

## 💡 Consejos Adicionales

- 🔒 **Seguridad:** Este sistema NO debe exponerse a Internet sin medidas de seguridad adicionales
- 📡 **Rendimiento:** Usar cable Ethernet en lugar de WiFi mejora la estabilidad
- 💾 **Backup:** Haz copias de seguridad de los datos de las citas
- 🔄 **Actualizaciones:** Mantén Java actualizado en ambas computadoras

---

## 📞 Soporte

Si sigues teniendo problemas después de seguir todos estos pasos:

1. Verifica que tienes Java 17 o superior: `java -version`
2. Compila el proyecto limpiamente: `mvn clean package`
3. Revisa los logs de errores completos
4. Verifica que no haya otros programas usando el puerto 1099

---

## 🔗 Puente TCP (NetworkBridgeAgent)

El agente `NetworkBridgeAgent` sincroniza los mensajes ACL entre las plataformas JADE cuando el doctor se ejecuta en otra computadora.

### Despliegue
- **Servidor (MainContainer)**: inicia el bridge en modo `SERVER` escuchando en el puerto `bridge.port` (6200 por defecto).
- **Cliente (RemoteContainer)**: levanta otro bridge en modo `CLIENT`, apuntando a `mainHost` y al mismo puerto.

### Propiedad `bridge.port`
- Cambiable con `-Dbridge.port=PUERTO` tanto en `MainContainer` como en `RemoteContainer`.
- Útil si 6200 ya está en uso o si se necesita un puerto autorizado distinto.

### Verificación rápida
1. Inicia `MainContainer` y confirma el mensaje `🕓 NetworkBridge esperando conexión en puerto ...`.
2. Desde la computadora remota ejecuta `Test-NetConnection -ComputerName <IP_SERVIDOR> -Port <PUERTO>`.
3. Si no conecta, revisa firewall/routers.

### Logs Clave
- `🔗 NetworkBridge enlazado...` → puente activo.
- `⏳ No se pudo conectar...` → cliente reintentando conexión.
- `🔁 entregó mensaje...` → tráfico JADE atravesando el socket.

Mantén ambos procesos abiertos; si el socket se cae, el agente intentará reconectarse automáticamente cada 3 segundos.
