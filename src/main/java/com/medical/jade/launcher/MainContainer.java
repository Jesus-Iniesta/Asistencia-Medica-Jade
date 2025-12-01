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

public class MainContainer {
    public static void main(String[] args) {
        try {
            // Configuración del contenedor principal
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();

            // Obtener la IP local REAL (evitando VirtualBox y loopback)
            // Usa -Dmain.host=IP_REAL si deseas forzar manualmente la IP detectada
            String configuredHost = System.getProperty("main.host");
            String localIP = configuredHost != null && !configuredHost.isBlank()
                    ? configuredHost.trim()
                    : detectPreferredLocalIP();
            int bridgePort = Integer.parseInt(System.getProperty(
                    "bridge.port",
                    String.valueOf(NetworkBridgeAgent.DEFAULT_PORT)));

            System.out.println("===========================================");
            System.out.println("🔍 DETECTANDO CONFIGURACIÓN DE RED...");
            System.out.println("===========================================");
            System.out.println("📍 IP detectada: " + localIP);
            System.out.println("🔌 Puerto socket puente: " + bridgePort);

            // Configuración CRÍTICA para conexiones remotas
            profile.setParameter(Profile.MAIN_HOST, localIP);
            profile.setParameter(Profile.LOCAL_HOST, localIP);  // ⬅️ IMPORTANTE para conexiones remotas
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.GUI, "true");
            profile.setParameter(Profile.PLATFORM_ID, "hospital-main");

            // Configuración adicional para aceptar conexiones remotas
            profile.setParameter(Profile.ACCEPT_FOREIGN_AGENTS, "true");

            // Crear contenedor principal
            ContainerController mainContainer = rt.createMainContainer(profile);

            // Crear puente de red vía sockets
            AgentController networkBridge = mainContainer.createNewAgent(
                    NetworkBridgeAgent.AGENT_NAME,
                    "com.medical.jade.agents.NetworkBridgeAgent",
                    new Object[]{NetworkBridgeAgent.Mode.SERVER.name(), bridgePort}
            );
            networkBridge.start();

            System.out.println("\n===========================================");
            System.out.println("🏥 COMPUTADORA PRINCIPAL - INICIADA");
            System.out.println("===========================================");
            System.out.println("📍 IP del Servidor: " + localIP);
            System.out.println("🔌 Puerto JADE: 1099");
            System.out.println("🌐 Puerto Web: 7070");
            System.out.println("🔗 Puerto Socket JADE Bridge: " + bridgePort);
            System.out.println("===========================================");

            System.out.println("\n📋 INSTRUCCIONES PARA COMPUTADORA SECUNDARIA:");
            System.out.println("   1. Abre RemoteContainer.java");
            System.out.println("   2. Cambia la línea 26 a:");
            System.out.println("      String mainHost = \"" + localIP + "\";");
            System.out.println("   3. Ajusta RemoteContainer para usar el puerto " + bridgePort + " (bridgePort)");
            System.out.println("   4. Ejecuta RemoteContainer");
            System.out.println("===========================================\n");

            // COMPUTADORA PRINCIPAL: Recepcionista + Enfermero
            AgentController recepcionista = mainContainer.createNewAgent(
                    "Recepcionista",
                    "com.medical.jade.agents.RecepcionistaAgent",
                    null
            );

            AgentController enfermero = mainContainer.createNewAgent(
                    "Enfermero",
                    "com.medical.jade.agents.EnfermeroAgent",
                    null
            );

            // Iniciar agentes
            recepcionista.start();
            enfermero.start();

            System.out.println("✅ AGENTES ACTIVOS EN COMPUTADORA PRINCIPAL:");
            System.out.println("   1. Recepcionista - Registra citas");
            System.out.println("   2. Enfermero - Toma signos vitales");
            System.out.println("   3. NetworkBridge - Encaminamiento TCP");
            System.out.println("\n⏳ Esperando conexión de Computadora Secundaria (Doctor)...\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR AL INICIAR CONTENEDOR PRINCIPAL:");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la IP local real, evitando direcciones loopback y VirtualBox
     */
    private static String detectPreferredLocalIP() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            String bestCandidate = null;
            int bestScore = -1;

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();

                if (!ni.isUp() || ni.isLoopback() || looksVirtual(ni)) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String hostAddress = addr.getHostAddress();

                    if (addr.isLoopbackAddress() || hostAddress.indexOf(':') >= 0) {
                        continue;
                    }
                    if (hostAddress.startsWith("169.254.")) {
                        continue;
                    }
                    if (hostAddress.startsWith("192.168.56.") || hostAddress.startsWith("192.168.122.")) {
                        continue;
                    }

                    int score = scoreAddress(hostAddress);
                    if (score > bestScore) {
                        bestScore = score;
                        bestCandidate = hostAddress;
                        if (score >= 4) {
                            return hostAddress; // Mejor escenario (172.16-31)
                        }
                    }
                }
            }

            if (bestCandidate != null) {
                return bestCandidate;
            }

            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();

            if (!ip.startsWith("127.") && !ip.startsWith("192.168.56.")) {
                return ip;
            }

            System.err.println("⚠️  No se detectó IP de red real. Usando localhost.");
            System.err.println("💡 Conecta a WiFi o Ethernet para usar en red.");
            return "localhost";

        } catch (Exception e) {
            System.err.println("⚠️  Error detectando IP, usando localhost");
            e.printStackTrace();
            return "localhost";
        }
    }

    private static boolean looksVirtual(NetworkInterface ni) {
        String display = ni.getDisplayName().toLowerCase();
        String name = ni.getName().toLowerCase();
        return display.contains("virtual") || display.contains("vmware") || display.contains("vbox") ||
               display.contains("hyper-v") || display.contains("docker") || display.contains("host-only") ||
               name.startsWith("vbox") || name.startsWith("vmnet") || name.startsWith("docker") ||
               name.startsWith("br-");
    }

    private static int scoreAddress(String hostAddress) {
        if (hostAddress.startsWith("172.")) {
            String[] octets = hostAddress.split("\\.");
            if (octets.length >= 2) {
                int second = Integer.parseInt(octets[1]);
                if (second >= 16 && second <= 31) {
                    return 4; // Rango privado 172.16/12
                }
            }
            return 3;
        }
        if (hostAddress.startsWith("10.")) {
            return 3;
        }
        if (hostAddress.startsWith("192.168.")) {
            return 2;
        }
        return 1; // Otros casos válidos
    }
}