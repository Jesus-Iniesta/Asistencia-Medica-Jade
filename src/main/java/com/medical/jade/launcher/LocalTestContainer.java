package com.medical.jade.launcher;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 * LocalTestContainer - Para probar el sistema en UNA SOLA computadora
 * Simula el comportamiento de dos computadoras usando contenedores separados
 */
public class LocalTestContainer {
    public static void main(String[] args) {
        try {
            // Configuración del contenedor remoto para PRUEBAS LOCALES
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();

            // Conectar a localhost para pruebas en la misma máquina
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.CONTAINER_NAME, "test-container");

            // Crear contenedor remoto (simulando la computadora secundaria)
            ContainerController testContainer = rt.createAgentContainer(profile);

            System.out.println("===========================================");
            System.out.println("🧪 CONTENEDOR DE PRUEBA LOCAL - INICIADO");
            System.out.println("===========================================");
            System.out.println("📍 Conectado a: localhost:1099");
            System.out.println("🎯 Modo: Simulación de computadora secundaria");
            System.out.println("===========================================\n");

            // CONTENEDOR DE PRUEBA: Doctor + Paciente (simulando computadora 2)
            AgentController doctor = testContainer.createNewAgent(
                    "Doctor",
                    "com.medical.jade.agents.DoctorAgent",
                    null
            );

            AgentController paciente = testContainer.createNewAgent(
                    "Paciente-P001",
                    "com.medical.jade.agents.PacienteAgent",
                    new Object[]{"P001", "María García", "Dolor de cabeza"}
            );

            // Iniciar agentes
            doctor.start();
            paciente.start();

            System.out.println("✅ AGENTES EN CONTENEDOR DE PRUEBA:");
            System.out.println("   1. Doctor - Realiza diagnósticos");
            System.out.println("   2. Paciente-P001 - Paciente de ejemplo");
            System.out.println("\n🎉 Sistema de prueba funcionando!");
            System.out.println("💡 Total: 4 agentes en 2 contenedores (misma computadora)\n");
            System.out.println("📝 Nota: Para producción usa RemoteContainer.java en computadora separada");

        } catch (Exception e) {
            System.err.println("===========================================");
            System.err.println("❌ ERROR AL INICIAR CONTENEDOR DE PRUEBA");
            System.err.println("===========================================");
            System.err.println("Asegúrate de que MainContainer esté ejecutándose primero");
            System.err.println("===========================================\n");
            e.printStackTrace();
        }
    }
}

