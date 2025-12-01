package com.medical.jade.launcher;

import com.medical.jade.agents.NetworkBridgeAgent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * RemoteContainer - Contenedor remoto para ejecutar en otra computadora
 * Este contenedor ejecuta el agente Doctor
 *
 * INSTRUCCIONES RÁPIDAS:
 * 1. Ejecuta MainContainer.java en la COMPUTADORA PRINCIPAL primero
 * 2. Copia la IP que muestra MainContainer
 * 3. Pégala en la variable 'mainHost' abajo (línea 29)
 * 4. Ejecuta este archivo
 * 5. El Doctor se conectará automáticamente
 */
public class RemoteContainer {
    public static void main(String[] args) {
        try {
            // ========================================
            // 🔧 CONFIGURACIÓN - Edita SOLO esta línea
            // ========================================

            // ⬇️ PEGA AQUÍ LA IP QUE MUESTRA MainContainer (usa ipconfig/ifconfig si es distinto)
            String mainHost = "10.211.172.68"; // ← Reemplaza con la IP real de la computadora principal
            int bridgePort = Integer.parseInt(System.getProperty(
                    "bridge.port",
                    String.valueOf(NetworkBridgeAgent.DEFAULT_PORT)));

            // Ejemplos:
            // String mainHost = "192.168.1.100";  // IP de la computadora principal
            // String mainHost = "10.0.0.5";       // Otra IP posible

            // ========================================

            // Detectar la IP local de ESTA computadora (evitando VirtualBox)
            String localIP = getRealLocalIP();

            System.out.println("\n===========================================");
            System.out.println("🔄 INICIANDO CONTENEDOR REMOTO...");
            System.out.println("===========================================");
            System.out.println("📍 IP de esta computadora: " + localIP);
            System.out.println("🔌 Intentando conectar a: " + mainHost + ":1099");
            System.out.println("🌐 Puerto socket puente: " + bridgePort);
            System.out.println("⏳ Esto puede tomar unos segundos...\n");

            // Configuración del contenedor remoto
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, mainHost);
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.LOCAL_HOST, localIP);  // ⬅️ CRÍTICO: IP local correcta
            profile.setParameter(Profile.CONTAINER_NAME, "remote-container");

            // Crear contenedor remoto
            ContainerController remoteContainer = rt.createAgentContainer(profile);

            System.out.println("✅ Conexión establecida con el MainContainer");
            System.out.println("===========================================\n");

            // Crear agente Doctor
            System.out.println("👨‍⚕️ Creando agente Doctor...");
            AgentController doctor = remoteContainer.createNewAgent(
                    "Doctor",
                    "com.medical.jade.agents.DoctorAgent",
                    null
            );

            doctor.start();

            // Crear bridge TCP en modo cliente hacia la computadora principal
            System.out.println("🌐 Conectando bridge TCP al host principal...");
            AgentController networkBridge = remoteContainer.createNewAgent(
                    NetworkBridgeAgent.AGENT_NAME,
                    "com.medical.jade.agents.NetworkBridgeAgent",
                    new Object[]{NetworkBridgeAgent.Mode.CLIENT.name(), mainHost, bridgePort}
            );
            networkBridge.start();

            System.out.println("===========================================");
            System.out.println("👨‍⚕️ Agente activo: Doctor");
            System.out.println("🔗 Bridge TCP activo en puerto remoto: " + bridgePort);
            System.out.println("===========================================");
            System.out.println("\n💡 El Doctor está listo para atender pacientes");
            System.out.println("🔗 Comunicándose con la computadora principal");
            System.out.println("\n⚠️  Mantén esta ventana abierta para que el Doctor siga activo\n");

        } catch (Exception e) {
            System.err.println("\n===========================================");
            System.err.println("❌ ERROR AL CONECTAR CON LA COMPUTADORA PRINCIPAL");
            System.err.println("===========================================");
            System.err.println("\n📝 CHECKLIST DE SOLUCIÓN:");
            System.err.println("   ❌ Verifica que MainContainer esté ejecutándose primero");
            System.err.println("   ❌ Asegúrate de copiar la IP CORRECTA que muestra MainContainer");
            System.err.println("   ❌ Verifica que ambas computadoras estén en la MISMA RED");
            System.err.println("   ❌ Verifica que el firewall permita el puerto 1099");
            System.err.println("\n🔥 SOLUCIÓN COMÚN DE FIREWALL:");
            System.err.println("   Windows:");
            System.err.println("      Panel de Control > Firewall > Permitir aplicación");
            System.err.println("      Agregar Java/javaw.exe a la lista");
            System.err.println("\n   Linux:");
            System.err.println("      sudo ufw allow 1099/tcp");
            System.err.println("      sudo firewall-cmd --add-port=1099/tcp (Fedora/CentOS)");
            System.err.println("\n🌐 VERIFICAR CONECTIVIDAD:");
            System.err.println("   Desde esta computadora, ejecuta:");
            System.err.println("      ping [IP_DE_COMPUTADORA_PRINCIPAL]");
            System.err.println("      telnet [IP_DE_COMPUTADORA_PRINCIPAL] 1099");
            System.err.println("===========================================\n");

            System.err.println("Detalles técnicos del error:");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la IP local real, evitando direcciones loopback y VirtualBox
     */
    private static String getRealLocalIP() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();

                // Saltar interfaces loopback, no activas, y VirtualBox
                if (ni.isLoopback() || !ni.isUp() || ni.getDisplayName().toLowerCase().contains("virtual")) {
                    continue;
                }

                // Preferir interfaces WiFi o Ethernet
                String niName = ni.getName().toLowerCase();
                boolean isPreferred = niName.startsWith("wlan") ||
                                     niName.startsWith("eth") ||
                                     niName.startsWith("en") ||
                                     niName.startsWith("wlp");

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // Buscar IPv4 que NO sea loopback ni VirtualBox
                    String hostAddress = addr.getHostAddress();
                    if (!addr.isLoopbackAddress() &&
                        hostAddress.indexOf(':') == -1 &&  // No IPv6
                        !hostAddress.startsWith("127.") &&
                        !hostAddress.startsWith("192.168.56.") && // VirtualBox
                        !hostAddress.startsWith("192.168.122.")) { // Otras VMs

                        if (isPreferred) {
                            return hostAddress; // Retornar inmediatamente si es una interfaz preferida
                        }
                    }
                }
            }

            // Fallback: intentar método estándar
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();

            if (!ip.startsWith("127.") && !ip.startsWith("192.168.56.")) {
                return ip;
            }

            // Último recurso
            System.err.println("⚠️  No se detectó IP de red real. Usando localhost.");
            System.err.println("💡 Conecta a WiFi o Ethernet para usar en red.");
            return "localhost";

        } catch (Exception e) {
            System.err.println("⚠️  Error detectando IP, usando localhost");
            e.printStackTrace();
            return "localhost";
        }
    }
}