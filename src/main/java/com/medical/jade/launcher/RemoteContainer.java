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
 * 1. Ejecuta MainContainer.java en la COMPUTADORA PRINCIPAL primero
 * 2. Copia la IP que muestra MainContainer
 * 3. Pégala en la variable 'mainHost' abajo (línea 26)
 * 4. Ejecuta este archivo
 * 5. El Doctor se conectará automáticamente
 */
public class RemoteContainer {
    public static void main(String[] args) {
        try {
            // ========================================
            // 🔧 CONFIGURACIÓN - Edita SOLO esta línea
            // ========================================

            // ⬇️ PEGA AQUÍ LA IP QUE MUESTRA MainContainer
            String mainHost = "172.26.49.144";

            // Ejemplos:
            // String mainHost = "192.168.1.100";  // IP de la computadora principal
            // String mainHost = "10.0.0.5";       // Otra IP posible

            // ========================================

            System.out.println("\n===========================================");
            System.out.println("🔄 INICIANDO CONTENEDOR REMOTO...");
            System.out.println("===========================================");
            System.out.println("🔌 Intentando conectar a: " + mainHost + ":1099");
            System.out.println("⏳ Esto puede tomar unos segundos...\n");

            // Configuración del contenedor remoto
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, mainHost);
            profile.setParameter(Profile.MAIN_PORT, "1099");
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

            System.out.println("\n===========================================");
            System.out.println("✅ CONTENEDOR REMOTO ACTIVO");
            System.out.println("===========================================");
            System.out.println("📍 Conectado a: " + mainHost);
            System.out.println("👨‍⚕️ Agente activo: Doctor");
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
}