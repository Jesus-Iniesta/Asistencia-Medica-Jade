package com.medical.jade.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.google.gson.Gson;
import com.medical.jade.messages.Cita;
import com.medical.jade.messages.Diagnostico;
import com.medical.jade.behaviours.*;

public class PacienteAgent extends Agent {
    private Gson gson = new Gson();
    private Diagnostico ultimoDiagnostico;
    private String pacienteId;
    private String nombre;
    private String sintomas;

    @Override
    protected void setup() {
        Object[] args = getArguments();

        if (args != null && args.length >= 3) {
            pacienteId = (String) args[0];
            nombre = (String) args[1];
            sintomas = (String) args[2];
        } else {
            pacienteId = "P001";
            nombre = "Paciente Desconocido";
            sintomas = "Consulta general";
        }

        System.out.println("✅ Paciente " + pacienteId + " (" + nombre + ") conectado al sistema");

        // Registrar servicio
        addBehaviour(new RegisterServiceBehaviour("paciente", "consulta-paciente"));

        // Recibir diagnósticos
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        addBehaviour(new ReceiveMessageBehaviour(mt, msg -> {
            recibirDiagnostico(msg);
        }));

        // 🔥 NUEVO: Enviar solicitud de cita al Recepcionista
        addBehaviour(new SearchServiceBehaviour("atencion-medica", agents -> {
            if (agents != null && agents.length > 0) {
                for (AID agent : agents) {
                    if (agent.getLocalName().contains("Recepcionista")) {
                        enviarSolicitudCita(agent);
                        break;
                    }
                }
            } else {
                System.err.println("❌ No se encontró Recepcionista disponible");
            }
        }));

        System.out.println("👤 Paciente " + pacienteId + " buscando Recepcionista...");
    }

    private void enviarSolicitudCita(AID recepcionista) {
        // Crear objeto cita
        Cita cita = new Cita();
        cita.setPacienteId(pacienteId);
        cita.setNombre(nombre);
        cita.setSintomas(sintomas);
        cita.setEstado("Solicitada");

        // Enviar al recepcionista
        addBehaviour(new SendResponseBehaviour(
            recepcionista,
            ACLMessage.REQUEST,
            gson.toJson(cita)
        ));

        System.out.println("📤 Paciente " + pacienteId + " envió solicitud de cita al Recepcionista");
        System.out.println("   Nombre: " + nombre);
        System.out.println("   Síntomas: " + sintomas);
    }

    private void recibirDiagnostico(ACLMessage msg) {
        System.out.println("\n📨 Paciente " + pacienteId + " recibió diagnóstico");

        try {
            ultimoDiagnostico = gson.fromJson(msg.getContent(), Diagnostico.class);

            System.out.println("=================================");
            System.out.println("📋 DIAGNÓSTICO MÉDICO");
            System.out.println("=================================");
            System.out.println("👤 Paciente ID: " + ultimoDiagnostico.getPacienteId());
            System.out.println("🩺 Diagnóstico: " + ultimoDiagnostico.getDiagnostico());
            System.out.println("💊 Tratamiento: " + ultimoDiagnostico.getTratamiento());
            System.out.println("📅 Próxima cita: " + ultimoDiagnostico.getFechaProxima());
            System.out.println("=================================\n");

            // Enviar confirmación de recepción
            addBehaviour(new SendResponseBehaviour(
                    msg.getSender(),
                    ACLMessage.CONFIRM,
                    "Diagnóstico recibido correctamente"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error al recibir diagnóstico: " + e.getMessage());
        }
    }

    public Diagnostico getUltimoDiagnostico() {
        return ultimoDiagnostico;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    @Override
    protected void takeDown() {
        System.out.println("👋 Paciente " + pacienteId + " desconectado del sistema");
    }
}