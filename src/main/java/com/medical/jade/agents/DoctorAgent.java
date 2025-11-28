package com.medical.jade.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.google.gson.Gson;
import com.medical.jade.messages.HistoriaClinica;
import com.medical.jade.messages.Diagnostico;
import com.medical.jade.behaviours.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DoctorAgent extends Agent {
    private Gson gson = new Gson();
    private int diagnosticosRealizados = 0;
    private static final HttpClient httpClient = HttpClient.newHttpClient();

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
                    diagnostico.setNombrePaciente(historia.getNombrePaciente());

                    String diagnosticoTexto = analizarSintomas(historia);
                    diagnostico.setDiagnostico(diagnosticoTexto);

                    String tratamientoTexto = prescribirTratamiento(historia);
                    diagnostico.setTratamiento(tratamientoTexto);
                    diagnostico.setFechaProxima(calcularProximaCita(historia));

                    // Asignar doctor especialista
                    String doctorInfo = asignarDoctorEspecialista(diagnosticoTexto);
                    String[] partes = doctorInfo.split("\\|");
                    diagnostico.setDoctorNombre(partes[0]);
                    diagnostico.setDoctorEspecialidad(partes[1]);

                    // 🔥 NUEVO: Generar mensaje personalizado del doctor
                    String mensajeDoctor = generarMensajePersonalizado(
                        historia.getNombrePaciente(),
                        partes[0],
                        diagnosticoTexto,
                        tratamientoTexto
                    );
                    diagnostico.setMensajeDoctor(mensajeDoctor);

                    System.out.println("\n✅ Diagnóstico completado:");
                    System.out.println("   👨‍⚕️ Doctor: " + diagnostico.getDoctorNombre());
                    System.out.println("   🎓 Especialidad: " + diagnostico.getDoctorEspecialidad());
                    System.out.println("   📋 " + diagnostico.getDiagnostico());
                    System.out.println("   💊 " + diagnostico.getTratamiento());
                    System.out.println("   📅 Próxima cita: " + diagnostico.getFechaProxima());

                    diagnosticosRealizados++;

                    // Guardar diagnóstico en el servidor web
                    guardarDiagnosticoEnWeb(diagnostico);

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

    /**
     * Guarda el diagnóstico en el servidor web vía HTTP POST
     */
    private void guardarDiagnosticoEnWeb(Diagnostico diagnostico) {
        try {
            String jsonDiagnostico = gson.toJson(diagnostico);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7070/api/diagnostico"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonDiagnostico))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("💾 Diagnóstico guardado en servidor web (ID: " +
                        diagnostico.getPacienteId() + ")");
            } else {
                System.err.println("⚠️ Error guardando en web: HTTP " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("⚠️ No se pudo guardar en servidor web: " + e.getMessage());
            // No es crítico, el diagnóstico ya se envió al paciente
        }
    }

    /**
     * Asigna el doctor especialista según la categoría del diagnóstico
     */
    private String asignarDoctorEspecialista(String diagnostico) {
        // Dr. Pedro Ramírez - Especialista en Medicina Interna y Gastroenterología
        if (diagnostico.contains("Gastro") || diagnostico.contains("Diarrea") ||
            diagnostico.contains("estómago") || diagnostico.contains("náuseas") ||
            diagnostico.contains("Intoxicación") || diagnostico.contains("Dispepsia") ||
            diagnostico.contains("Estreñimiento")) {
            return "Dr. Pedro Ramírez|Medicina Interna y Gastroenterología";
        }

        // Dra. Carmen Flores - Especialista en Neumología y Medicina Respiratoria
        if (diagnostico.contains("respiratori") || diagnostico.contains("Tos") ||
            diagnostico.contains("Faringitis") || diagnostico.contains("Bronquitis") ||
            diagnostico.contains("pulmonar") || diagnostico.contains("Rinitis") ||
            diagnostico.contains("garganta")) {
            return "Dra. Carmen Flores|Neumología y Medicina Respiratoria";
        }

        // Dr. Miguel Ángel Torres - Especialista en Cardiología y Neurología
        if (diagnostico.contains("cardio") || diagnostico.contains("presión") ||
            diagnostico.contains("Hipertensión") || diagnostico.contains("pecho") ||
            diagnostico.contains("Taquicardia") || diagnostico.contains("Cefalea") ||
            diagnostico.contains("Migraña") || diagnostico.contains("vértigo") ||
            diagnostico.contains("Hipotensión") || diagnostico.contains("angina")) {
            return "Dr. Miguel Ángel Torres|Cardiología y Neurología";
        }

        // Dr. Pedro Ramírez - Medicina General (por defecto)
        return "Dr. Pedro Ramírez|Medicina General";
    }

    private String analizarSintomas(HistoriaClinica historia) {
        String sintomas = historia.getSintomas().toLowerCase();
        double temperatura = historia.getTemperatura();
        String[] presion = historia.getPresionArterial().split("/");
        int sistolica = Integer.parseInt(presion[0]);
        int diastolica = Integer.parseInt(presion[1]);

        // === ANÁLISIS CARDIOVASCULAR ===
        if (sintomas.contains("dolor en el pecho") || sintomas.contains("dolor pecho")) {
            if (sistolica > 140 || sistolica < 90) {
                return "Posible angina de pecho - Requiere atención cardiológica urgente";
            }
            return "Dolor torácico - Requiere evaluación cardiovascular";
        }

        if (sintomas.contains("palpitaciones") || sintomas.contains("taquicardia")) {
            return "Taquicardia - Alteración del ritmo cardíaco";
        }

        // === ANÁLISIS GASTROINTESTINAL ===
        if (sintomas.contains("náuseas") || sintomas.contains("nauseas")) {
            if (sintomas.contains("vómito") || sintomas.contains("vomito")) {
                if (sintomas.contains("diarrea")) {
                    return "Gastroenteritis aguda - Infección gastrointestinal";
                }
                if (sintomas.contains("fiebre") || temperatura > 38.0) {
                    return "Intoxicación alimentaria - Posible infección bacteriana";
                }
                return "Síndrome emético - Náuseas y vómitos";
            }
            return "Dispepsia - Malestar digestivo";
        }

        if (sintomas.contains("dolor de estómago") || sintomas.contains("dolor estomago") ||
            sintomas.contains("dolor abdominal")) {
            if (sintomas.contains("diarrea")) {
                return "Gastroenteritis - Inflamación gastrointestinal";
            }
            if (sintomas.contains("ardor") || sintomas.contains("acidez")) {
                return "Gastritis aguda - Inflamación de la mucosa gástrica";
            }
            return "Dolor abdominal - Requiere evaluación digestiva";
        }

        if (sintomas.contains("diarrea")) {
            if (temperatura > 38.0) {
                return "Diarrea infecciosa - Probable infección intestinal";
            }
            return "Diarrea aguda - Alteración del tránsito intestinal";
        }

        if (sintomas.contains("estreñimiento") || sintomas.contains("constipación")) {
            return "Estreñimiento - Tránsito intestinal lento";
        }

        // === ANÁLISIS RESPIRATORIO ===
        if (temperatura > 37.5) {
            if (sintomas.contains("tos")) {
                if (sintomas.contains("flema") || sintomas.contains("mucosidad")) {
                    return "Bronquitis aguda - Infección de vías respiratorias bajas";
                }
                if (sintomas.contains("dificultad para respirar") || sintomas.contains("falta de aire")) {
                    return "Infección respiratoria con compromiso pulmonar - Requiere atención";
                }
                return "Infección respiratoria aguda con proceso febril";
            }
            if (sintomas.contains("dolor de garganta")) {
                return "Faringoamigdalitis aguda - Infección de vías respiratorias altas";
            }
            return "Proceso febril - Probable infección viral";
        }

        if (sintomas.contains("tos")) {
            if (sintomas.contains("seca")) {
                return "Tos seca persistente - Posible irritación bronquial";
            }
            return "Tos - Irritación de vías respiratorias";
        }

        if (sintomas.contains("dolor de garganta") || sintomas.contains("dolor garganta")) {
            return "Faringitis aguda - Inflamación de vías respiratorias altas";
        }

        if (sintomas.contains("congestión") || sintomas.contains("nariz tapada")) {
            return "Rinitis - Congestión nasal";
        }

        // === ANÁLISIS NEUROLÓGICO ===
        if (sintomas.contains("dolor de cabeza") || sintomas.contains("cefalea")) {
            if (sintomas.contains("intenso") || sintomas.contains("fuerte")) {
                return "Cefalea intensa - Requiere evaluación neurológica";
            }
            if (sintomas.contains("náuseas") || sintomas.contains("vómito")) {
                return "Migraña - Cefalea con síntomas asociados";
            }
            return "Cefalea tensional - Posible estrés o fatiga";
        }

        if (sintomas.contains("mareo") || sintomas.contains("vértigo")) {
            return "Síndrome vertiginoso - Alteración del equilibrio";
        }

        // === ANÁLISIS MUSCULOESQUELÉTICO ===
        if (sintomas.contains("dolor muscular") || sintomas.contains("dolor de cuerpo")) {
            if (temperatura > 37.5) {
                return "Mialgia febril - Probable proceso viral";
            }
            return "Mialgia - Dolor muscular";
        }

        if (sintomas.contains("dolor articular") || sintomas.contains("dolor en las articulaciones")) {
            return "Artralgia - Dolor articular";
        }

        // === ANÁLISIS DERMATOLÓGICO ===
        if (sintomas.contains("erupción") || sintomas.contains("sarpullido") || sintomas.contains("ronchas")) {
            if (sintomas.contains("picazón") || sintomas.contains("comezón")) {
                return "Reacción alérgica cutánea - Dermatitis";
            }
            return "Erupción cutánea - Requiere evaluación dermatológica";
        }

        // === ANÁLISIS CARDIOVASCULAR - PRESIÓN ===
        if (sistolica > 140 || diastolica > 90) {
            return "Hipertensión arterial - Presión elevada, requiere control";
        }

        if (sistolica < 90 || diastolica < 60) {
            return "Hipotensión arterial - Presión baja";
        }

        // === OTROS SÍNTOMAS ===
        if (sintomas.contains("fatiga") || sintomas.contains("cansancio")) {
            return "Astenia - Fatiga generalizada";
        }

        if (sintomas.contains("fiebre") && temperatura > 37.5) {
            return "Síndrome febril - Proceso infeccioso";
        }

        // === CHEQUEO GENERAL ===
        return "Chequeo general - Estado de salud estable";
    }

    private String prescribirTratamiento(HistoriaClinica historia) {
        String diagnostico = analizarSintomas(historia);

        // === TRATAMIENTOS CARDIOVASCULARES ===
        if (diagnostico.contains("angina") || diagnostico.contains("dolor torácico")) {
            return "⚠️ URGENTE: Acudir a urgencias inmediatamente. Nitroglicerina sublingual si está prescrita";
        }

        if (diagnostico.contains("Taquicardia")) {
            return "Beta bloqueador según prescripción, evitar cafeína y alcohol, control cardiológico";
        }

        // === TRATAMIENTOS GASTROINTESTINALES ===
        if (diagnostico.contains("Gastroenteritis aguda")) {
            return "Suero oral (rehidratación), dieta blanda BRAT (banano, arroz, manzana, tostadas), probióticos. Loperamida si es necesario";
        }

        if (diagnostico.contains("Intoxicación alimentaria")) {
            return "Hidratación abundante con suero oral, dieta líquida las primeras 24h, reposo absoluto. Si persiste vómito: Metoclopramida 10mg";
        }

        if (diagnostico.contains("Gastritis")) {
            return "Omeprazol 20mg en ayunas por 14 días, dieta blanda sin irritantes (café, alcohol, picante), evitar AINEs";
        }

        if (diagnostico.contains("Diarrea")) {
            return "Suero oral de rehidratación, Loperamida 2mg después de cada deposición, probióticos, dieta astringente";
        }

        if (diagnostico.contains("Estreñimiento")) {
            return "Aumentar fibra (20-30g/día), hidratación abundante (2L agua/día), ejercicio moderado, Lactulosa si persiste";
        }

        if (diagnostico.contains("Dispepsia") || diagnostico.contains("emético")) {
            return "Omeprazol 20mg antes de comidas, Metoclopramida 10mg si náuseas, comidas pequeñas y frecuentes";
        }

        // === TRATAMIENTOS RESPIRATORIOS ===
        if (diagnostico.contains("Bronquitis")) {
            return "Ambroxol 30mg c/8h (expectorante), abundantes líquidos, reposo, humidificador ambiental. Si fiebre: Paracetamol";
        }

        if (diagnostico.contains("compromiso pulmonar")) {
            return "⚠️ Antibiótico (Azitromicina 500mg/día por 5 días), broncodilatador si dificultad respiratoria, reposo absoluto";
        }

        if (diagnostico.contains("Infección respiratoria")) {
            return "Amoxicilina 500mg c/8h por 7 días, antiinflamatorio (Ibuprofeno 400mg c/8h), abundantes líquidos, reposo";
        }

        if (diagnostico.contains("Faringoamigdalitis")) {
            return "Antibiótico (Amoxicilina 500mg c/8h por 7-10 días), analgésico, gárgaras con agua tibia y sal";
        }

        if (diagnostico.contains("Faringitis")) {
            return "Antiinflamatorio (Ibuprofeno 400mg c/8h), gárgaras con agua sal 3 veces/día, pastillas para garganta, líquidos abundantes";
        }

        if (diagnostico.contains("Tos seca")) {
            return "Dextrometorfano 15mg c/8h, miel con limón, evitar irritantes, humidificador nocturno";
        }

        if (diagnostico.contains("Rinitis")) {
            return "Descongestionante nasal (Oximetazolina máx 3 días), antihistamínico (Loratadina 10mg/día), lavados nasales con suero";
        }

        // === TRATAMIENTOS NEUROLÓGICOS ===
        if (diagnostico.contains("Cefalea intensa")) {
            return "⚠️ Ibuprofeno 600mg o Naproxeno 500mg. Si no mejora en 2h o empeora: acudir a urgencias";
        }

        if (diagnostico.contains("Migraña")) {
            return "Sumatriptán 50mg al inicio del dolor, reposo en ambiente oscuro y silencioso, compresas frías, evitar triggers";
        }

        if (diagnostico.contains("Cefalea tensional")) {
            return "Ibuprofeno 400mg c/8h si es necesario, relajación muscular, reducir estrés, hidratación adecuada";
        }

        if (diagnostico.contains("vertiginoso")) {
            return "Dimenhidrinato 50mg c/8h, reposo, evitar movimientos bruscos, hidratación";
        }

        // === TRATAMIENTOS MUSCULOESQUELÉTICOS ===
        if (diagnostico.contains("Mialgia febril")) {
            return "Paracetamol 500mg c/6h, reposo, hidratación abundante, compresas tibias en zonas dolorosas";
        }

        if (diagnostico.contains("Mialgia") || diagnostico.contains("Artralgia")) {
            return "Ibuprofeno 400mg c/8h por 5 días, reposo relativo, aplicar calor local, estiramientos suaves";
        }

        // === TRATAMIENTOS DERMATOLÓGICOS ===
        if (diagnostico.contains("alérgica")) {
            return "Antihistamínico (Loratadina 10mg/día), crema de hidrocortisona 1% en zona afectada, evitar alérgeno identificado";
        }

        if (diagnostico.contains("Erupción cutánea")) {
            return "Crema hidratante, evitar rascado, compresas frías si hay inflamación, consultar dermatología";
        }

        // === TRATAMIENTOS PRESIÓN ARTERIAL ===
        if (diagnostico.contains("Hipertensión")) {
            return "Antihipertensivo (Losartán 50mg/día en la mañana), dieta DASH (baja en sal <2g/día), ejercicio aeróbico 30min/día";
        }

        if (diagnostico.contains("Hipotensión")) {
            return "Aumentar ingesta de líquidos y sal, levantarse lentamente, medias de compresión, evitar ayunos prolongados";
        }

        // === TRATAMIENTOS GENERALES ===
        if (diagnostico.contains("febril") || diagnostico.contains("Proceso febril")) {
            return "Paracetamol 500mg c/6h si fiebre >38°C, abundantes líquidos (2-3L/día), reposo, compresas tibias";
        }

        if (diagnostico.contains("Astenia")) {
            return "Complejo vitamínico B, dieta balanceada rica en hierro, descanso adecuado (7-8h), ejercicio moderado";
        }

        // === CHEQUEO GENERAL ===
        return "Complejo vitamínico, hidratación adecuada (2L/día), alimentación balanceada, ejercicio regular 30min/día";
    }

    private String calcularProximaCita(HistoriaClinica historia) {
        String diagnostico = analizarSintomas(historia);

        // === URGENCIAS ===
        if (diagnostico.contains("URGENTE") || diagnostico.contains("angina") ||
            diagnostico.contains("compromiso pulmonar")) {
            return "🚨 ACUDIR A URGENCIAS INMEDIATAMENTE";
        }

        // === SEGUIMIENTO CORTO (1 SEMANA) ===
        if (diagnostico.contains("Intoxicación") || diagnostico.contains("Gastroenteritis") ||
            diagnostico.contains("Bronquitis") || diagnostico.contains("Infección") ||
            diagnostico.contains("Diarrea infecciosa")) {
            return "En 1 semana para verificar evolución";
        }

        // === SEGUIMIENTO MEDIO (2-3 SEMANAS) ===
        if (diagnostico.contains("Hipertensión") || diagnostico.contains("Migraña") ||
            diagnostico.contains("Gastritis") || diagnostico.contains("Faringoamigdalitis")) {
            return "En 2 semanas para control y ajuste de tratamiento";
        }

        // === SEGUIMIENTO MEDIO (1 MES) ===
        if (diagnostico.contains("Cefalea intensa") || diagnostico.contains("Taquicardia") ||
            diagnostico.contains("Erupción") || diagnostico.contains("Astenia")) {
            return "En 1 mes para evaluación de respuesta al tratamiento";
        }

        // === SEGUIMIENTO LARGO (3 MESES) ===
        return "En 3 meses para chequeo de rutina y prevención";
    }

    /**
     * Genera un mensaje personalizado del doctor al paciente
     */
    private String generarMensajePersonalizado(String nombrePaciente, String nombreDoctor, String diagnostico, String tratamiento) {
        return String.format(
            "Hola %s, soy el Dr. %s. He revisado tu caso y tengo las siguientes recomendaciones:\n\n" +
            "Diagnóstico: %s\n" +
            "Tratamiento: %s\n\n" +
            "Es importante que sigas estas indicaciones y asistas a tu próxima cita. Cuídate!",
            nombrePaciente != null ? nombrePaciente : "paciente",
            nombreDoctor,
            diagnostico,
            tratamiento.toLowerCase()
        );
    }

    @Override
    protected void takeDown() {
        System.out.println("👋 Doctor " + getLocalName() + " finalizando...");
        System.out.println("📊 Total de diagnósticos realizados: " + diagnosticosRealizados);
    }
}
