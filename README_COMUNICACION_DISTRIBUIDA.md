# Guía de Comunicación Distribuida con JADE + Socket Bridge

Esta guía describe cómo ejecutar el sistema con dos computadoras intercambiando mensajes JADE mediante el `NetworkBridgeAgent`.

## 1. Requisitos
- Java 21 instalado en ambas máquinas.
- Maven disponible en `PATH` **o** uso de tu IDE para ejecutar las clases launcher.
- Red local que permita tráfico TCP en los puertos `1099` (JADE) y `6200` (puente, configurable).
- Firewall actualizado para permitir conexiones entrantes/salientes en esos puertos.

## 2. Computadora Principal (Recepcionista + Enfermero + Bridge)
1. Obtén la IP local real (el propio `MainContainer` la imprime al inicio).
2. (Opcional) Define el puerto del bridge si necesitas uno distinto:
   ```powershell
   $env=_('')
   ```
3. Ejecuta el contenedor principal:
   ```powershell
   cd D:\repos\Java\Asistencia-Medica-Jade
   mvn -q exec:java -Dexec.mainClass=com.medical.jade.launcher.MainContainer
   ```
   Si Maven no está disponible, ejecuta `MainContainer` desde tu IDE.
4. Conserva en pantalla los datos impresos:
   - IP detectada (por ejemplo `192.168.1.50`).
   - Puerto socket bridge (por defecto `6200` o el que hayas definido vía `-Dbridge.port`).

## 3. Computadora Secundaria (Doctor + Bridge Cliente)
1. Copia el proyecto o el artefacto ejecutable.
2. Edita `src/main/java/com/medical/jade/launcher/RemoteContainer.java` y actualiza `mainHost` con la IP del paso anterior.
3. Si en la principal cambiaste el puerto del bridge, ejecuta el remoto con la misma propiedad (`-Dbridge.port=<puerto>`).
4. Arranca el contenedor remoto:
   ```powershell
   cd D:\repos\Java\Asistencia-Medica-Jade
   mvn -q exec:java -Dexec.mainClass=com.medical.jade.launcher.RemoteContainer -Dbridge.port=6200
   ```
   Alternativamente, usa tu IDE para ejecutar `RemoteContainer`.
5. Verifica que la consola muestre `🔗 Bridge TCP activo…` indicando que el socket se enlazó con el servidor.

## 4. Flujo de prueba recomendado
1. Con ambos contenedores activos, inicia el servidor web/interfaz si lo usas habitualmente (`WebInterfaceServer`).
2. Crea un paciente (desde la interfaz o levantando un `PacienteAgent`).
3. Observa en la computadora principal:
   - Recepcionista recibe la cita.
   - Si no hay enfermero local registrado, se mostrará `🌐 Cita enviada al doctor remoto vía bridge`.
4. En la computadora secundaria, el Doctor debe mostrar `🌐 Mensaje recibido desde puente remoto` y procesar la historia.
5. Confirma que el diagnóstico vuelve al paciente y se guarda en el servidor web (si está disponible).

## 5. Personalización y consejos
- **Puerto del bridge**: pasa `-Dbridge.port=<puerto>` al lanzar ambos contenedores si necesitas evitar conflictos.
- **Nombres de agentes**: por defecto se usa `Doctor`. Si cambiaste el nombre remoto, pasa ese valor como argumento al crear `Recepcionista`/`Enfermero` para que sepan a quién reenviar.
- **Firewall**: abre manualmente los puertos 1099 y el del bridge en ambos equipos (Entrada y Salida, TCP).
- **Diagnóstico de red**:
  ```powershell
  Test-NetConnection 192.168.1.50 -Port 6200   # Windows PowerShell
  ping 192.168.1.50                            # Alcance ICMP
  ```
- **Logs**: busca en consola los mensajes con emoji `🌐` para confirmar que los paquetes cruzan el puente.
- **Detener el sistema**: cierra las ventanas de los launchers; el `NetworkBridgeAgent` se apagará automáticamente.

Con esto tendrás dos plataformas JADE coordinadas mediante sockets estándar, manteniendo sincronizados los agentes de recepción/enfermería con el doctor remoto.

