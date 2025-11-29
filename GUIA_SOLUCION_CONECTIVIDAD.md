# 🔧 Guía Rápida: Solución al Error "No existe ninguna ruta hasta el host"

## 🎯 Problema

Al intentar conectar dos computadoras, aparece este error:

```
jade.imtp.leap.ICPException: Error creating connection.
Caused by: No existe ninguna ruta hasta el `host'
```

---

## ✅ Solución en 3 Pasos

### 1️⃣ Configura el Firewall (CRÍTICO)

Este es el problema **más común**. El firewall bloquea las conexiones entrantes.

#### En Linux (tu caso):

```bash
# Permitir el puerto de JADE
sudo ufw allow 1099/tcp
sudo ufw allow 7070/tcp
sudo ufw reload

# Verificar que las reglas se agregaron
sudo ufw status
```

**Salida esperada:**
```
Estado: activo

Para                       Acción      Desde
----                       ------      -----
1099/tcp                   ALLOW       Anywhere
7070/tcp                   ALLOW       Anywhere
```

#### Solución Rápida (para pruebas):

```bash
# Desactivar firewall temporalmente para confirmar que es el problema
sudo ufw disable

# Intentar conectar las computadoras

# Si funciona, el problema ERA el firewall
# Vuelve a activarlo y agrega las reglas:
sudo ufw enable
sudo ufw allow 1099/tcp
sudo ufw allow 7070/tcp
```

---

### 2️⃣ Verifica que MainContainer detecta la IP correcta

#### Al ejecutar MainContainer, debe mostrar:

```
===========================================
🔍 DETECTANDO CONFIGURACIÓN DE RED...
===========================================
📍 IP detectada: 192.168.X.X
```

⚠️ **IMPORTANTE:** La IP **NO** debe ser:
- ❌ `127.0.0.1` (localhost)
- ❌ `127.0.1.1` (localhost)
- ❌ `192.168.56.X` (VirtualBox)
- ❌ `192.168.122.X` (libvirt/KVM)

✅ **Debe ser la IP de tu WiFi/Ethernet real**, por ejemplo:
- ✅ `192.168.1.100`
- ✅ `10.0.0.5`
- ✅ `172.26.49.144`

#### Si detecta una IP incorrecta:

El código actualizado de `MainContainer.java` ya filtra automáticamente las interfaces virtuales. Si aún así detecta mal:

```bash
# Ver todas tus interfaces de red
ip addr show

# Busca la interfaz correcta (wlan0, eth0, enp3s0, etc.)
# Y anota su IP
```

---

### 3️⃣ Usa la IP correcta en RemoteContainer

1. **Copia** la IP que muestra MainContainer
2. **Abre** `RemoteContainer.java`
3. **Edita** la línea 26:

```java
String mainHost = "192.168.1.100";  // Pega TU IP aquí
```

4. **Guarda** el archivo
5. **Compila** (si usas IDE, recompila automáticamente)
6. **Ejecuta** RemoteContainer

---

## 🧪 Verificar Conectividad ANTES de ejecutar JADE

### Desde la Computadora Secundaria:

```bash
# 1. Verificar ping
ping 192.168.1.100

# Deberías ver:
# 64 bytes from 192.168.1.100: icmp_seq=1 ttl=64 time=2.5 ms
```

Si el **ping falla**:
- Ambas computadoras NO están en la misma red
- Verifica que ambas estén conectadas a la misma WiFi
- Verifica que no haya "Aislamiento de cliente" en el router

Si el **ping funciona**, verifica el puerto:

```bash
# 2. Verificar puerto (después de iniciar MainContainer)
telnet 192.168.1.100 1099

# Deberías ver:
# Trying 192.168.1.100...
# Connected to 192.168.1.100.
```

Si **telnet falla** pero ping funciona:
- El problema ES el firewall
- Sigue el paso 1 de esta guía

---

## 📝 Checklist Completo

Antes de intentar conectar, verifica:

- [ ] MainContainer está ejecutándose en la computadora principal
- [ ] MainContainer muestra una IP válida (no 127.x ni 192.168.56.x)
- [ ] Firewall permite el puerto 1099 (`sudo ufw allow 1099/tcp`)
- [ ] Ambas computadoras están en la misma red WiFi
- [ ] Ping funciona entre las computadoras
- [ ] Telnet al puerto 1099 funciona
- [ ] RemoteContainer.java tiene la IP correcta en la línea 26
- [ ] El proyecto está compilado (`mvn clean package`)

---

## 🎬 Orden de Ejecución Correcto

### En la Computadora Principal:

```bash
# Terminal 1: MainContainer
java -cp target/classes com.medical.jade.launcher.MainContainer

# Espera a que muestre: "⏳ Esperando conexión de Computadora Secundaria..."

# Terminal 2: WebInterfaceServer
java -cp target/classes com.medical.jade.launcher.WebInterfaceServer
```

### En la Computadora Secundaria:

```bash
# Asegúrate de que MainContainer YA esté corriendo
java -cp target/classes com.medical.jade.launcher.RemoteContainer
```

---

## 🔍 Diagnóstico: ¿Por qué falla?

### Revisa la salida de MainContainer:

#### ✅ CORRECTO:
```
📍 IP detectada: 192.168.1.100
📍 IP del Servidor: 192.168.1.100
```

#### ❌ INCORRECTO:
```
📍 IP detectada: 127.0.1.1
⚠️ No se detectó IP de red real. Usando localhost.
```

Si ves el mensaje incorrecto:
1. Conecta tu computadora a WiFi o Ethernet
2. Verifica con: `ip addr show | grep inet`
3. Asegúrate de que NO uses VirtualBox activo

---

## 🚀 Solución Definitiva

Si ya hiciste todo y sigue sin funcionar:

### 1. Reinicia todo desde cero:

```bash
# En la Computadora Principal
# Detén todos los procesos Java (Ctrl+C)

# Limpia y recompila
mvn clean package

# Inicia MainContainer
java -cp target/classes com.medical.jade.launcher.MainContainer
```

### 2. En la Computadora Secundaria:

```bash
# Verifica conectividad PRIMERO
ping [IP_DE_PRINCIPAL]
telnet [IP_DE_PRINCIPAL] 1099

# Si ambos funcionan, ejecuta:
java -cp target/classes com.medical.jade.launcher.RemoteContainer
```

---

## 💡 Configuración Alternativa: Usar IP Fija

Si tu IP cambia constantemente (DHCP):

### Configurar IP estática (Linux):

```bash
# Edita la configuración de red
sudo nano /etc/netplan/01-network-manager-all.yaml

# Agrega:
network:
  version: 2
  renderer: NetworkManager
  ethernets:
    enp3s0:  # Tu interfaz
      dhcp4: no
      addresses:
        - 192.168.1.100/24
      gateway4: 192.168.1.1
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]

# Aplica cambios
sudo netplan apply
```

---

## 📞 Última Opción: Usar en la Misma Computadora

Si no logras conectar dos computadoras, puedes probar el sistema en una sola:

```bash
# Terminal 1: MainContainer
java -cp target/classes com.medical.jade.launcher.MainContainer

# Terminal 2: LocalTestContainer (en lugar de RemoteContainer)
java -cp target/classes com.medical.jade.launcher.LocalTestContainer

# Terminal 3: WebInterfaceServer
java -cp target/classes com.medical.jade.launcher.WebInterfaceServer
```

Esto creará todos los agentes en la misma computadora para que puedas probar el sistema.

---

## ✅ Confirmación de Éxito

Sabrás que funcionó cuando veas:

### En MainContainer:
```
✅ AGENTES ACTIVOS EN COMPUTADORA PRINCIPAL:
   1. Recepcionista - Registra citas
   2. Enfermero - Toma signos vitales

⏳ Esperando conexión de Computadora Secundaria (Doctor)...
```

### En RemoteContainer:
```
✅ Conexión establecida con el MainContainer
===========================================

👨‍⚕️ Creando agente Doctor...

===========================================
✅ CONTENEDOR REMOTO ACTIVO
===========================================
📍 Conectado a: 192.168.1.100
👨‍⚕️ Agente activo: Doctor
```

### En la GUI de JADE:
Verás **4 agentes** (incluyendo el AMS y DF de JADE):
- ams (JADE)
- df (JADE)
- Recepcionista
- Enfermero
- **Doctor** ← Este viene de la computadora secundaria

---

¡Listo! Si sigues esta guía paso a paso, deberías poder conectar las dos computadoras sin problemas.

