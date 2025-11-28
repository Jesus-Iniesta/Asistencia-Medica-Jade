package com.medical.jade.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.google.gson.Gson;
import com.medical.jade.messages.Cita;
import com.medical.jade.messages.HistoriaClinica;
import com.medical.jade.behaviours.*;

public class EnfermeroAgent extends Agent {
    private Gson gson = new Gson();
    private int pacientesAtendidos = 0;

    @Override
    protected void setup() {
        System.out.println("✅ Enfermero " + getLocalName() + " está listo");

        // Registrar servicio
        addBehaviour(new RegisterServiceBehaviour("atencion-medica", "enfermeria"));

        // Monitor de actividad
        MonitorAgentBehaviour monitor = new MonitorAgentBehaviour(this, 30000);
        addBehaviour(monitor);

        // Recibir pacientes de recepción
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mt, msg -> {
            tomarSignosVitales(msg, monitor);
        }));

        System.out.println("💉 Enfermero esperando pacientes...");
    }

    private void tomarSignosVitales(ACLMessage msg, MonitorAgentBehaviour monitor) {
        String contenido = msg.getContent();
        System.out.println("\n📥 Enfermero recibió paciente");

        try {
            Cita cita = gson.fromJson(contenido, Cita.class);

            System.out.println("🩺 Tomando signos vitales de: " + cita.getNombre());

            // Simular toma de signos vitales (delay)
            addBehaviour(new ProcessRequestBehaviour(msg, request -> {
                try {
                    Thread.sleep(2000); // Simular tiempo de atención

                    // Crear historia clínica
                    HistoriaClinica historia = new HistoriaClinica();
                    historia.setPacienteId(cita.getPacienteId());
                    historia.setNombrePaciente(cita.getNombre()); // 🔥 NUEVO: Pasar nombre del paciente
                    historia.setPresionArterial(generarPresion());
                    historia.setTemperatura(generarTemperatura());
                    historia.setFrecuenciaCardiaca(generarFrecuencia());
                    historia.setSintomas(cita.getSintomas());

                    System.out.println("📋 Signos vitales registrados:");
                    System.out.println("   - Presión: " + historia.getPresionArterial());
                    System.out.println("   - Temperatura: " + historia.getTemperatura() + "°C");
                    System.out.println("   - Frecuencia: " + historia.getFrecuenciaCardiaca() + " lpm");

                    pacientesAtendidos++;

                    // Buscar doctor y enviar historia
                    addBehaviour(new SearchServiceBehaviour("atencion-medica", agents -> {
                        for (AID agent : agents) {
                            if (agent.getLocalName().contains("Doctor")) {
                                addBehaviour(new SendResponseBehaviour(
                                        agent,
                                        ACLMessage.REQUEST,
                                        gson.toJson(historia)
                                ));
                                System.out.println("✉️ Historia clínica enviada al Doctor\n");
                                break;
                            }
                        }
                    }));

                    monitor.incrementMessageCount();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

        } catch (Exception e) {
            System.err.println("❌ Error en enfermería: " + e.getMessage());
        }
    }

    private String generarPresion() {
        int sistolica = 110 + (int)(Math.random() * 30);
        int diastolica = 70 + (int)(Math.random() * 20);
        return sistolica + "/" + diastolica;
    }

    private double generarTemperatura() {
        return 36.0 + (Math.random() * 2);
    }

    private int generarFrecuencia() {
        return 60 + (int)(Math.random() * 40);
    }

    @Override
    protected void takeDown() {
        System.out.println("👋 Enfermero " + getLocalName() + " finalizando...");
        System.out.println("📊 Total de pacientes atendidos: " + pacientesAtendidos);
    }
}