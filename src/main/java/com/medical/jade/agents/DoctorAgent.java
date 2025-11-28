package com.medical.jade.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.google.gson.Gson;
import com.medical.jade.messages.HistoriaClinica;
import com.medical.jade.messages.Diagnostico;
import com.medical.jade.behaviours.*;

public class DoctorAgent extends Agent {
    private Gson gson = new Gson();
    private int diagnosticosRealizados = 0;

    @Override
    protected void setup() {
        System.out.println("✅ Doctor " + getLocalName() + " está listo");

        // Registrar servicio
        addBehaviour(new RegisterServiceBehaviour("atencion-medica", "consulta-medica"));

        // Monitor de actividad
        MonitorAgentBehaviour monitor = new MonitorAgentBehaviour(this, 30000);
        addBehaviour(monitor);

        // Recibir historias clínicas
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        addBehaviour(new ReceiveMessageBehaviour(mt, msg -> {
            realizarDiagnostico(msg, monitor);
        }));

        System.out.println("👨‍⚕️ Doctor esperando pacientes...");
    }

    private void realizarDiagnostico(ACLMessage msg, MonitorAgentBehaviour monitor) {
        String contenido = msg.getContent();
        System.out.println("\n📥 Doctor recibió historia clínica");

        try {
            HistoriaClinica historia = gson.fromJson(contenido, HistoriaClinica.class);

            System.out.println("🔍 Analizando paciente ID: " + historia.getPacienteId());
            System.out.println("📊 Signos vitales:");
            System.out.println("   - Presión: " + historia.getPresionArterial());
            System.out.println("   - Temperatura: " + historia.getTemperatura() + "°C");
            System.out.println("   - Frecuencia: " + historia.getFrecuenciaCardiaca() + " lpm");
            System.out.println("   - Síntomas: " + historia.getSintomas());

            // Simular análisis médico
            addBehaviour(new ProcessRequestBehaviour(msg, request -> {
                try {
                    Thread.sleep(3000); // Simular tiempo de diagnóstico

                    // Realizar diagnóstico basado en síntomas y signos
                    Diagnostico diagnostico = new Diagnostico();
                    diagnostico.setPacienteId(historia.getPacienteId());
                    diagnostico.setDiagnostico(analizarSintomas(historia));
                    diagnostico.setTratamiento(prescribirTratamiento(historia));
                    diagnostico.setFechaProxima(calcularProximaCita(historia));

                    System.out.println("\n✅ Diagnóstico completado:");
                    System.out.println("   📋 " + diagnostico.getDiagnostico());
                    System.out.println("   💊 " + diagnostico.getTratamiento());
                    System.out.println("   📅 Próxima cita: " + diagnostico.getFechaProxima());

                    diagnosticosRealizados++;

                    // Enviar diagnóstico al paciente
                    AID pacienteAID = new AID("Paciente-" + historia.getPacienteId(), AID.ISLOCALNAME);
                    addBehaviour(new SendResponseBehaviour(
                            pacienteAID,
                            ACLMessage.INFORM,
                            gson.toJson(diagnostico)
                    ));

                    System.out.println("✉️ Diagnóstico enviado al Paciente\n");

                    monitor.incrementMessageCount();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

        } catch (Exception e) {
            System.err.println("❌ Error en diagnóstico: " + e.getMessage());
        }
    }

    private String analizarSintomas(HistoriaClinica historia) {
        String sintomas = historia.getSintomas().toLowerCase();

        // Análisis de temperatura
        if (historia.getTemperatura() > 37.5) {
            if (sintomas.contains("tos") || sintomas.contains("gripe")) {
                return "Infección respiratoria aguda con proceso febril";
            }
            return "Proceso febril - Probable infección viral";
        }

        // Análisis de presión arterial
        String[] presion = historia.getPresionArterial().split("/");
        int sistolica = Integer.parseInt(presion[0]);
        if (sistolica > 140) {
            return "Hipertensión arterial - Requiere control";
        }

        // Análisis de síntomas específicos
        if (sintomas.contains("dolor de cabeza") || sintomas.contains("cefalea")) {
            return "Cefalea tensional - Posible estrés o fatiga";
        }

        if (sintomas.contains("dolor abdominal") || sintomas.contains("estómago")) {
            return "Gastritis o dispepsia - Requiere dieta especial";
        }

        if (sintomas.contains("dolor") && sintomas.contains("garganta")) {
            return "Faringitis aguda - Inflamación de vías respiratorias";
        }

        return "Chequeo general - Estado de salud estable";
    }

    private String prescribirTratamiento(HistoriaClinica historia) {
        String diagnostico = analizarSintomas(historia);

        if (diagnostico.contains("Infección respiratoria")) {
            return "Antibiótico (Amoxicilina 500mg c/8h por 7 días), antiinflamatorio y reposo";
        }

        if (diagnostico.contains("febril")) {
            return "Paracetamol 500mg c/6h, abundantes líquidos y reposo";
        }

        if (diagnostico.contains("Hipertensión")) {
            return "Antihipertensivo (Losartán 50mg diario), dieta baja en sal, ejercicio moderado";
        }

        if (diagnostico.contains("Cefalea")) {
            return "Ibuprofeno 400mg c/8h si persiste, reducir estrés, hidratación";
        }

        if (diagnostico.contains("Gastritis")) {
            return "Omeprazol 20mg en ayunas, dieta blanda, evitar irritantes";
        }

        if (diagnostico.contains("Faringitis")) {
            return "Antiinflamatorio, gárgaras con agua sal, caramelos para garganta";
        }

        return "Vitaminas, hidratación adecuada y alimentación balanceada";
    }

    private String calcularProximaCita(HistoriaClinica historia) {
        String diagnostico = analizarSintomas(historia);

        if (diagnostico.contains("Hipertensión") || diagnostico.contains("control")) {
            return "En 2 semanas para control";
        }

        if (diagnostico.contains("Infección") || diagnostico.contains("febril")) {
            return "En 1 semana si los síntomas persisten";
        }

        return "En 3 meses para chequeo de rutina";
    }

    @Override
    protected void takeDown() {
        System.out.println("👋 Doctor " + getLocalName() + " finalizando...");
        System.out.println("📊 Total de diagnósticos realizados: " + diagnosticosRealizados);
    }
}