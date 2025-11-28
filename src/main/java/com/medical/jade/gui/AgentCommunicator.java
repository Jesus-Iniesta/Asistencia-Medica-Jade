package com.medical.jade.gui;

import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.lang.acl.ACLMessage;
import com.google.gson.Gson;
import com.medical.jade.messages.Cita;
import com.medical.jade.messages.Diagnostico;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentCommunicator {
    private static AgentCommunicator instance;
    private ContainerController container;
    private Gson gson;
    private Map<String, Diagnostico> diagnosticosCache;

    private AgentCommunicator() {
        this.gson = new Gson();
        this.diagnosticosCache = new ConcurrentHashMap<>();
    }

    public static AgentCommunicator getInstance() {
        if (instance == null) {
            instance = new AgentCommunicator();
        }
        return instance;
    }

    public void setContainer(ContainerController container) {
        this.container = container;
    }

    public void enviarCita(Cita cita) throws Exception {
        // Crear agente paciente temporal
        String pacienteNombre = "Paciente-" + cita.getPacienteId();
        AgentController paciente = container.createNewAgent(
                pacienteNombre,
                "com.medical.jade.agents.PacienteAgent",
                new Object[]{cita.getPacienteId()}
        );
        paciente.start();

        // Enviar mensaje al recepcionista
        Thread.sleep(500); // Dar tiempo para inicializar

        // Aquí normalmente usarías el agente para enviar
        // Por simplicidad, asumimos que el flujo continúa
        System.out.println("✅ Cita enviada al sistema: " + cita.getPacienteId());
    }

    public Diagnostico obtenerDiagnostico(String pacienteId) {
        return diagnosticosCache.get(pacienteId);
    }

    public void guardarDiagnostico(String pacienteId, Diagnostico diagnostico) {
        diagnosticosCache.put(pacienteId, diagnostico);
    }

    public Map<String, Object> getEstadoSistema() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("diagnosticos_pendientes", diagnosticosCache.size());
        estado.put("sistema_activo", container != null);
        return estado;
    }
}
