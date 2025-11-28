# 🌐 Configuración para Dos Computadoras

## 📋 Distribución de Agentes

### **COMPUTADORA PRINCIPAL** (MainContainer)
- **Agente 1**: Recepcionista - Registra citas y pacientes
- **Agente 2**: Enfermero - Toma signos vitales

### **COMPUTADORA SECUNDARIA** (RemoteContainer)
- **Agente 1**: Doctor - Realiza diagnósticos
- **Agente 2**: Paciente - Representa al paciente en el sistema

**Total**: 4 agentes distribuidos en 2 computadoras ✅

---

## 🚀 Instrucciones de Configuración

### **PASO 1: Configurar Computadora Principal**

1. Abre `MainContainer.java`
2. Ejecuta el programa
3. **Copia la IP que aparece en la consola**, por ejemplo:
   ```
   📍 Host Principal: 192.168.1.100
   ```

### **PASO 2: Configurar Computadora Secundaria**

1. Abre `RemoteContainer.java`
2. Busca la línea:
   ```java
   String mainHost = "192.168.1.XXX";
   ```
3. Reemplaza `192.168.1.XXX` con la IP de la computadora principal:
   ```java
   String mainHost = "192.168.1.100";  // IP de tu computadora principal
   ```
4. Guarda el archivo
5. Ejecuta `RemoteContainer.java`

---

## 🧪 Pruebas en UNA SOLA Computadora (Desarrollo)

Si quieres probar el sistema sin tener dos computadoras físicas:

1. **Terminal 1**: Ejecuta `MainContainer.java`
2. **Terminal 2**: Ejecuta `LocalTestContainer.java` (NO RemoteContainer)

`LocalTestContainer` simula una segunda computadora usando `localhost`.

---

## 🔧 Verificación de Red

### Antes de ejecutar, verifica:

✅ **Misma Red**: Ambas computadoras deben estar en la misma red WiFi/Ethernet

✅ **Firewall**: Permite el puerto 1099 en ambas computadoras
   - Windows: `netsh advfirewall firewall add rule name="JADE" dir=in action=allow protocol=TCP localport=1099`
   - Linux: `sudo ufw allow 1099/tcp`
   - macOS: Sistema > Seguridad > Firewall > Opciones > Permitir puerto 1099

✅ **Ping**: Desde la computadora secundaria, haz ping a la principal
   ```bash
   ping 192.168.1.100
   ```

---

## 📊 Orden de Ejecución

### Para DOS COMPUTADORAS:
```
1️⃣ COMPUTADORA PRINCIPAL → MainContainer.java
2️⃣ COMPUTADORA SECUNDARIA → RemoteContainer.java
```

### Para UNA COMPUTADORA (pruebas):
```
1️⃣ Terminal 1 → MainContainer.java
2️⃣ Terminal 2 → LocalTestContainer.java
```

---

## 🐛 Solución de Problemas

### Error: "No ICP active"
**Causa**: Configuración incorrecta de red
**Solución**: NO uses `localhost` en MainContainer cuando trabajes con dos computadoras

### Error: "Name-clash Agent already present"
**Causa**: Los agentes ya existen en la plataforma
**Solución**: 
1. Cierra todos los contenedores
2. Borra los archivos `APDescription.txt` y `MTPs-Main-Container.txt`
3. Vuelve a ejecutar MainContainer primero

### Error: "Connection refused"
**Causa**: La computadora principal no está accesible
**Solución**:
1. Verifica que MainContainer esté ejecutándose
2. Verifica la IP en RemoteContainer
3. Revisa el firewall

### Error: "Unexpected token 'N', Not Found"
**Causa**: Falta la dependencia Jackson
**Solución**: Ejecuta `mvn clean install` para instalar dependencias

---

## 📝 Ejemplo de Configuración Completa

### Escenario: Hospital con 2 estaciones

**Estación 1 (Recepción) - IP: 192.168.1.100**
- Ejecuta: `MainContainer.java`
- Agentes: Recepcionista, Enfermero
- Funciones: Registrar pacientes, tomar signos vitales

**Estación 2 (Consultorio) - IP: 192.168.1.101**
- Ejecuta: `RemoteContainer.java` (configurado con 192.168.1.100)
- Agentes: Doctor, Paciente
- Funciones: Diagnósticos, atención médica

---

## 🔍 Verificar Conexión Exitosa

Cuando todo funciona correctamente, verás:

**En MainContainer:**
```
✅ AGENTES EN COMPUTADORA PRINCIPAL:
   1. Recepcionista - Registra citas
   2. Enfermero - Toma signos vitales

⏳ Esperando conexión de Computadora Secundaria...
```

**En RemoteContainer:**
```
✅ AGENTES EN COMPUTADORA SECUNDARIA:
   1. Doctor - Realiza diagnósticos
   2. Paciente-P001 - Paciente de ejemplo

🎉 Sistema distribuido funcionando correctamente!
💡 Total: 4 agentes en 2 computadoras
```

---

## 📞 Soporte

Si encuentras problemas:
1. Revisa que ambas computadoras estén en la misma red
2. Verifica que el firewall permita el puerto 1099
3. Asegúrate de ejecutar MainContainer ANTES que RemoteContainer
4. Revisa los logs de JADE para errores específicos

