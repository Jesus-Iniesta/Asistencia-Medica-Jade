package com.medical.jade.launcher;

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
            String localIP = getRealLocalIP();

            System.out.println("===========================================");
            System.out.println("🔍 DETECTANDO CONFIGURACIÓN DE RED...");
            System.out.println("===========================================");
            System.out.println("📍 IP detectada: " + localIP);

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

            System.out.println("\n===========================================");
            System.out.println("🏥 COMPUTADORA PRINCIPAL - INICIADA");
            System.out.println("===========================================");
            System.out.println("📍 IP del Servidor: " + localIP);
            System.out.println("🔌 Puerto JADE: 1099");
            System.out.println("🌐 Puerto Web: 7070");
            System.out.println("===========================================");

            System.out.println("\n📋 INSTRUCCIONES PARA COMPUTADORA SECUNDARIA:");
            System.out.println("   1. Abre RemoteContainer.java");
            System.out.println("   2. Cambia la línea 26 a:");
            System.out.println("      String mainHost = \"" + localIP + "\";");
            System.out.println("   3. Ejecuta RemoteContainer");
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
            System.out.println("\n⏳ Esperando conexión de Computadora Secundaria (Doctor)...\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR AL INICIAR CONTENEDOR PRINCIPAL:");
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