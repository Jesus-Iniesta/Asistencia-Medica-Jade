package com.medical.jade.launcher;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class RemoteContainer {
    public static void main(String[] args) {
        try {
            // Configuración del contenedor remoto
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();

            // ⚠️ IMPORTANTE: Cambia esta IP a la de la COMPUTADORA PRINCIPAL
            // Ejemplo: Si la computadora principal tiene IP 192.168.1.100, usa esa IP
            String mainHost = "192.168.1.XXX"; // ⬅️ CAMBIA AQUÍ a la IP de la computadora principal

            // Para probar en la MISMA computadora (solo desarrollo):
            // String mainHost = "localhost";

            profile.setParameter(Profile.MAIN_HOST, mainHost);
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.CONTAINER_NAME, "remote-container");

            // NO configurar LOCAL_HOST - JADE lo detecta automáticamente

            // Crear contenedor remoto
            ContainerController remoteContainer = rt.createAgentContainer(profile);

            System.out.println("===========================================");
            System.out.println("🏥 COMPUTADORA SECUNDARIA - INICIADA");
            System.out.println("===========================================");
            System.out.println("📍 Conectado a: " + mainHost + ":1099");
            System.out.println("===========================================\n");

            // COMPUTADORA SECUNDARIA: Doctor + Paciente (ejemplo)
            AgentController doctor = remoteContainer.createNewAgent(
                    "Doctor",
                    "com.medical.jade.agents.DoctorAgent",
                    null
            );

            AgentController paciente = remoteContainer.createNewAgent(
                    "Paciente-P001",
                    "com.medical.jade.agents.PacienteAgent",
                    new Object[]{"P001", "Juan Pérez", "Fiebre y tos"}
            );

            // Iniciar agentes
            doctor.start();
            paciente.start();

            System.out.println("✅ AGENTES EN COMPUTADORA SECUNDARIA:");
            System.out.println("   1. Doctor - Realiza diagnósticos");
            System.out.println("   2. Paciente-P001 - Paciente de ejemplo");
            System.out.println("\n🎉 Sistema distribuido funcionando correctamente!");
            System.out.println("💡 Total: 4 agentes en 2 computadoras\n");

        } catch (Exception e) {
            System.err.println("===========================================");
            System.err.println("❌ ERROR AL CONECTAR CON COMPUTADORA PRINCIPAL");
            System.err.println("===========================================");
            System.err.println("Verifica que:");
            System.err.println("1. ✅ MainContainer esté ejecutándose en la computadora principal");
            System.err.println("2. ✅ Hayas configurado la IP correcta en 'mainHost'");
            System.err.println("3. ✅ El puerto 1099 esté abierto en el firewall");
            System.err.println("4. ✅ Ambas computadoras estén en la misma red");
            System.err.println("===========================================\n");
            e.printStackTrace();
        }
    }
}