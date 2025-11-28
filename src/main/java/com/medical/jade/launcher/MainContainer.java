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

            // Obtener la IP local REAL (no 127.0.0.1 ni 127.0.1.1)
            String localIP = getRealLocalIP();

            // Determinar si usar localhost o IP real
            boolean useLocalhost = localIP.equals("127.0.0.1") || localIP.equals("127.0.1.1");
            String hostToUse = useLocalhost ? "localhost" : localIP;

            // Configuración de red para COMPUTADORA PRINCIPAL
            profile.setParameter(Profile.MAIN_HOST, hostToUse);
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.GUI, "true");
            profile.setParameter(Profile.PLATFORM_ID, "hospital-main");

            // Si usamos localhost, configurar LOCAL_HOST también
            if (useLocalhost) {
                profile.setParameter(Profile.LOCAL_HOST, "localhost");
            }

            // Crear contenedor principal
            ContainerController mainContainer = rt.createMainContainer(profile);

            System.out.println("===========================================");
            System.out.println("🏥 COMPUTADORA PRINCIPAL - INICIADA");
            System.out.println("===========================================");
            System.out.println("📍 Host Principal: " + hostToUse);
            System.out.println("🔌 Puerto: 1099");

            if (useLocalhost) {
                System.out.println("⚠️  Modo: Pruebas locales (localhost)");
                System.out.println("💡 Para usar en red, conecta a WiFi/Ethernet");
            } else {
                System.out.println("🌐 Modo: Red (accesible desde otras computadoras)");
            }

            System.out.println("===========================================");
            System.out.println("📋 INSTRUCCIONES PARA COMPUTADORA SECUNDARIA:");

            if (useLocalhost) {
                System.out.println("   ⚠️  En la MISMA computadora:");
                System.out.println("   1. Ejecuta LocalTestContainer.java");
            } else {
                System.out.println("   📡 En OTRA computadora:");
                System.out.println("   1. Abre RemoteContainer.java");
                System.out.println("   2. Cambia mainHost a: " + hostToUse);
                System.out.println("   3. Ejecuta RemoteContainer");
            }

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

            System.out.println("✅ AGENTES EN COMPUTADORA PRINCIPAL:");
            System.out.println("   1. Recepcionista - Registra citas");
            System.out.println("   2. Enfermero - Toma signos vitales");
            System.out.println("\n⏳ Esperando conexión de Computadora Secundaria (Doctor + Paciente)...\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR AL INICIAR CONTENEDOR PRINCIPAL:");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la IP local real, evitando direcciones loopback
     */
    private static String getRealLocalIP() {
        try {
            // Primero intentar obtener la IP por el método estándar
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();

            // Si NO es una dirección loopback, usarla
            if (!ip.startsWith("127.")) {
                return ip;
            }

            // Si es loopback, buscar la primera interfaz de red real
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();

                // Saltar interfaces loopback y no activas
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // Buscar IPv4 que NO sea loopback
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        return addr.getHostAddress();
                    }
                }
            }

            // Si no se encontró ninguna IP real, usar localhost
            return "127.0.0.1";

        } catch (Exception e) {
            System.err.println("⚠️  Error detectando IP, usando localhost");
            return "127.0.0.1";
        }
    }
}