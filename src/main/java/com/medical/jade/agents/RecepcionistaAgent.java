package com.medical.jade.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.google.gson.Gson;
import com.medical.jade.messages.Cita;
import com.medical.jade.behaviours.*;
import com.medical.jade.network.RemoteMessagingService;

public class RecepcionistaAgent extends Agent {
    private Gson gson = new Gson();
    private int turnosAsignados = 0;
    private String remoteDoctorName = "Doctor";

    @Override
    protected void setup() {
        System.out.println("✅ Recepcionista " + getLocalName() + " está listo");
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof String target) {
            remoteDoctorName = target;
        }

        // Registrar servicio en el DF (Directory Facilitator)
        addBehaviour(new RegisterServiceBehaviour("atencion-medica", "recepcion"));

        // Monitorear actividad cada 30 segundos
        MonitorAgentBehaviour monitor = new MonitorAgentBehaviour(this, 30000);
        addBehaviour(monitor);

        // Behaviour principal: recibir solicitudes de citas
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mt, msg -> {
            procesarSolicitudCita(msg, monitor);
        }));

        System.out.println("📋 Recepcionista esperando pacientes...");
    }

    private void procesarSolicitudCita(ACLMessage msg, MonitorAgentBehaviour monitor) {
        String contenido = msg.getContent();
        System.out.println("\n📥 Recepcionista recibió solicitud: " + contenido);

        try {
            // Parsear solicitud
            Cita cita = gson.fromJson(contenido, Cita.class);

            // Asignar turno y procesar
            turnosAsignados++;
            cita.setNumeroTurno(turnosAsignados);
            cita.setEstado("Registrado");

            System.out.println("🎫 Turno asignado: " + turnosAsignados);
            System.out.println("👤 Paciente: " + cita.getNombre());

            // Responder al solicitante
            addBehaviour(new SendResponseBehaviour(
                    msg.getSender(),
                    ACLMessage.INFORM,
                    gson.toJson(cita)
            ));

            // Buscar enfermero disponible y enviar
            addBehaviour(new SearchServiceBehaviour("atencion-medica", agents -> {
                for (AID agent : agents) {
                    if (agent.getLocalName().contains("Enfermero")) {
                        addBehaviour(new SendResponseBehaviour(
                                agent,
                                ACLMessage.REQUEST,
                                gson.toJson(cita)
                        ));
                        System.out.println("✉️ Cita enviada al Enfermero");
                        return;
                    }
                }

                // Si no hay enfermeros locales, reenviar al remoto via socket
                RemoteMessagingService.sendRemote(this,
                        remoteDoctorName,
                        ACLMessage.REQUEST,
                        gson.toJson(cita));
                System.out.println("🌐 Cita enviada al doctor remoto vía bridge");
            }));

            monitor.incrementMessageCount();

        } catch (Exception e) {
            System.err.println("❌ Error procesando solicitud: " + e.getMessage());
            addBehaviour(new SendResponseBehaviour(
                    msg.getSender(),
                    ACLMessage.FAILURE,
                    "Error al procesar solicitud"
            ));
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("👋 Recepcionista " + getLocalName() + " finalizando...");
        System.out.println("📊 Total de turnos asignados: " + turnosAsignados);
    }
}