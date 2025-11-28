# 📘 Instrucciones de Uso - Sistema Médico Distribuido

## 🖥️ Simulación en la Misma Computadora

Tu sistema está ahora configurado para ejecutarse en **una sola computadora** simulando dos contenedores distribuidos.

### Pasos para Ejecutar:

#### 1️⃣ Iniciar el Contenedor Principal
```bash
# En una terminal o desde IntelliJ, ejecuta:
MainContainer.java
```

**Qué hace:**
- Crea el contenedor principal JADE en `localhost:1099`
- Inicia los agentes: `Recepcionista` y `Enfermero`
- Abre la GUI de JADE (interfaz gráfica)
- Queda esperando conexiones de otros contenedores

**Deberías ver:**
```
=================================
🏥 CONTENEDOR PRINCIPAL INICIADO
=================================
📍 Host: localhost
🔌 Puerto: 1099
🌐 IP Local: [tu IP]
=================================

✅ Recepcionista iniciado
✅ Enfermero iniciado

⏳ Esperando conexión del contenedor remoto...
```

#### 2️⃣ Iniciar el Contenedor Remoto
```bash
# En OTRA terminal o pestaña de IntelliJ, ejecuta:
RemoteContainer.java
```

**Qué hace:**
- Se conecta al contenedor principal en `localhost:1099`
- Inicia los agentes: `Doctor` y `Paciente-P001`
- Los agentes de ambos contenedores pueden comunicarse entre sí

**Deberías ver:**
```
=================================
🏥 CONTENEDOR REMOTO INICIADO
=================================
📍 Conectado a: localhost:1099
🌐 Modo: Simulación local (misma computadora)
=================================

✅ Doctor iniciado
✅ Paciente iniciado

🎉 Sistema distribuido funcionando correctamente!
💡 Los 4 agentes están corriendo en contenedores separados
```

### 📊 Verificación

En la **GUI de JADE** (ventana gráfica) deberías ver:
- **Contenedor Principal**: Recepcionista, Enfermero
- **remote-container**: Doctor, Paciente-P001

Los 4 agentes pueden comunicarse entre sí mediante mensajes ACL.

---

## 🌐 Para Usar en Dos Computadoras Diferentes

### En la Computadora 1 (Servidor - Contenedor Principal):

1. **Edita `MainContainer.java`:**
```java
// Descomenta estas líneas:
String localIP = InetAddress.getLocalHost().getHostAddress();
profile.setParameter(Profile.LOCAL_HOST, localIP);
```

2. **Obtén tu IP local:**
```bash
ip addr show | grep "inet " | grep -v 127.0.0.1
# O en Windows: ipconfig
```

3. **Configura el firewall** para permitir conexiones en el puerto 1099

4. **Ejecuta MainContainer.java**

### En la Computadora 2 (Cliente - Contenedor Remoto):

1. **Edita `RemoteContainer.java`:**
```java
// Cambia localhost por la IP de la Computadora 1:
String mainHost = "192.168.1.100"; // IP de la Computadora 1

// Si tienes problemas de conexión, descomenta:
// String localIP = InetAddress.getLocalHost().getHostAddress();
// profile.setParameter(Profile.LOCAL_HOST, localIP);
```

2. **Asegúrate de estar en la misma red** que la Computadora 1

3. **Ejecuta RemoteContainer.java**

---

## ❓ Solución de Problemas

### Error: "No ICP active"
- **Causa**: Configuración incorrecta de IP o puerto
- **Solución**: Usa `localhost` para pruebas locales, no configures `LOCAL_HOST`

### Error: "Cannot invoke ... because mainContainer is null"
- **Causa**: El contenedor principal no se pudo crear
- **Solución**: Verifica que no haya otro proceso usando el puerto 1099

### Error en RemoteContainer: No puede conectar
- **Causa**: El MainContainer no está ejecutándose
- **Solución**: Inicia primero MainContainer, luego RemoteContainer

### Verificar que el puerto 1099 esté libre:
```bash
# Linux/Mac:
lsof -i :1099

# Windows:
netstat -ano | findstr :1099
```

---

## 🔧 Configuración Actual

✅ **Modo actual**: Simulación en una computadora
- MainContainer: `localhost:1099`
- RemoteContainer: Se conecta a `localhost:1099`
- **NO** requiere configuración de red
- Funciona inmediatamente

---

## 📝 Notas

- **Orden importante**: Siempre inicia primero `MainContainer`, luego `RemoteContainer`
- **GUI de JADE**: Te permite ver todos los agentes y sus mensajes
- **Logs**: Revisa la consola de cada contenedor para ver la actividad
- **Puerto predeterminado**: 1099 (puedes cambiarlo si es necesario)

