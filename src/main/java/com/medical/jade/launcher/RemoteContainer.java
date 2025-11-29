package com.medical.jade.launcher;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 * RemoteContainer - Contenedor remoto para ejecutar en otra computadora
 * Este contenedor ejecuta el agente Doctor
 *
 * INSTRUCCIONES RÁPIDAS:
 * 1. Edita la variable 'mainHost' con la IP de la computadora principal
 * 2. Ejecuta este archivo
 * 3. El Doctor se conectará automáticamente al sistema
 */
public class RemoteContainer {
    public static void main(String[] args) {
        try {
            // ========================================
            // 🔧 CONFIGURACIÓN - Edita solo esta línea
            // ========================================

            // OPCIÓN A: Prueba en la MISMA computadora (desarrollo)
            String mainHost = "localhost";

            // OPCIÓN B: Otra computadora en la red
            // Descomentar y cambiar XXX por la IP real de la computadora principal
            // Ejemplo: String mainHost = "192.168.1.100";
            // String mainHost = "192.168.1.XXX";

            // ========================================

            System.out.println("\n===========================================");
            System.out.println("🔄 INICIANDO CONTENEDOR REMOTO...");
            System.out.println("===========================================");
            System.out.println("🔌 Conectando a: " + mainHost + ":1099");

            // Configuración del contenedor remoto
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, mainHost);
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.CONTAINER_NAME, "remote-container");

            // Crear contenedor remoto
            ContainerController remoteContainer = rt.createAgentContainer(profile);

            System.out.println("✅ Conectado exitosamente");
            System.out.println("===========================================\n");

            // Crear agente Doctor
            System.out.println("👨‍⚕️ Creando agente Doctor...");
            AgentController doctor = remoteContainer.createNewAgent(
                    "Doctor",
                    "com.medical.jade.agents.DoctorAgent",
                    null
            );

            doctor.start();

            System.out.println("\n===========================================");
            System.out.println("✅ CONTENEDOR REMOTO ACTIVO");
            System.out.println("===========================================");
            System.out.println("📍 Host principal: " + mainHost);
            System.out.println("👨‍⚕️ Agente activo: Doctor");
            System.out.println("===========================================");
            System.out.println("\n💡 El Doctor está listo para atender pacientes");
            System.out.println("🔗 Comunicándose con la computadora principal");
            System.out.println("\n⚠️  Mantén esta ventana abierta para que el Doctor siga activo\n");

        } catch (Exception e) {
            System.err.println("\n===========================================");
            System.err.println("❌ ERROR AL CONECTAR");
            System.err.println("===========================================");
            System.err.println("\n📝 CHECKLIST DE SOLUCIÓN:");
            System.err.println("   □ MainContainer está ejecutándose en la computadora principal");
            System.err.println("   □ La IP en 'mainHost' es correcta");
            System.err.println("   □ El puerto 1099 está abierto en el firewall");
            System.err.println("   □ Ambas computadoras están en la misma red");
            System.err.println("\n💡 TIP: Para pruebas locales, usa mainHost = \"localhost\"");
            System.err.println("===========================================\n");

            System.err.println("Detalles del error:");
            e.printStackTrace();
        }
    }
}