package com.medical.jade.gui;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import com.google.gson.Gson;
import com.medical.jade.messages.Cita;
import com.medical.jade.messages.Diagnostico;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer {
    private static ContainerController container;
    private static final Gson gson = new Gson();
    private static final Map<String, Diagnostico> diagnosticosCache = new ConcurrentHashMap<>();
    private static final Map<String, Cita> citasEnProceso = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Iniciar JADE
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "1099");
        p.setParameter(Profile.GUI, "true");

        container = rt.createMainContainer(p);

        try {
            // Crear solo los agentes del contenedor principal
            // El Doctor se creará en el RemoteContainer
            AgentController recep = container.createNewAgent("Recepcionista",
                    "com.medical.jade.agents.RecepcionistaAgent", null);
            AgentController enf = container.createNewAgent("Enfermero",
                    "com.medical.jade.agents.EnfermeroAgent", null);

            recep.start();
            enf.start();

            System.out.println("✅ Agentes del contenedor principal iniciados correctamente");
            System.out.println("   - Recepcionista");
            System.out.println("   - Enfermero");
            System.out.println("⏳ Esperando que RemoteContainer inicie al Doctor...");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Servidor web
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/webapp", Location.CLASSPATH);
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(7070);

        // Endpoint para registrar cita
        app.post("/api/cita", ctx -> {
            try {
                String body = ctx.body();
                Cita cita = gson.fromJson(body, Cita.class);

                System.out.println("📋 Nueva cita recibida para: " + cita.getNombre());

                // Guardar cita en proceso
                citasEnProceso.put(cita.getPacienteId(), cita);

                // Crear agente paciente que iniciará el flujo
                String pacienteNombre = "Paciente-" + cita.getPacienteId();
                AgentController paciente = container.createNewAgent(
                        pacienteNombre,
                        "com.medical.jade.agents.PacienteAgent",
                        new Object[]{cita.getPacienteId(), cita.getNombre(), cita.getSintomas()}
                );
                paciente.start();

                System.out.println("✅ Agente paciente " + pacienteNombre + " creado");

                // Simular el flujo completo de diagnóstico
                new Thread(() -> procesarCitaSimulada(cita)).start();

                // Usar gson explícitamente para evitar problemas de serialización
                String jsonResponse = gson.toJson(Map.of(
                    "status", "success",
                    "message", "Cita registrada correctamente",
                    "pacienteId", cita.getPacienteId()
                ));

                ctx.contentType("application/json").result(jsonResponse);

            } catch (Exception e) {
                e.printStackTrace();
                String errorResponse = gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al procesar la cita: " + e.getMessage()
                ));
                ctx.status(500).contentType("application/json").result(errorResponse);
            }
        });

        // Endpoint para obtener diagnóstico
        app.get("/api/diagnostico/{pacienteId}", ctx -> {
            String pacienteId = ctx.pathParam("pacienteId");

            Diagnostico diagnostico = diagnosticosCache.get(pacienteId);

            if (diagnostico != null) {
                System.out.println("✅ Diagnóstico encontrado para paciente: " + pacienteId);
                String jsonResponse = gson.toJson(diagnostico);
                ctx.contentType("application/json").result(jsonResponse);
            } else {
                System.out.println("⏳ Diagnóstico aún no disponible para: " + pacienteId);
                String jsonResponse = gson.toJson(Map.of(
                    "status", "pending",
                    "message", "Diagnóstico en proceso"
                ));
                ctx.contentType("application/json").result(jsonResponse);
            }
        });

        // Endpoint para agregar diagnóstico (usado por los agentes)
        app.post("/api/diagnostico", ctx -> {
            try {
                String body = ctx.body();
                Diagnostico diagnostico = gson.fromJson(body, Diagnostico.class);

                diagnosticosCache.put(diagnostico.getPacienteId(), diagnostico);
                System.out.println("✅ Diagnóstico guardado para: " + diagnostico.getPacienteId());

                String jsonResponse = gson.toJson(Map.of("status", "success"));
                ctx.contentType("application/json").result(jsonResponse);
            } catch (Exception e) {
                String errorResponse = gson.toJson(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
                ctx.status(500).contentType("application/json").result(errorResponse);
            }
        });

        // Endpoint de health check
        app.get("/api/health", ctx -> {
            String jsonResponse = gson.toJson(Map.of(
                "status", "ok",
                "agentes", "activos",
                "citasEnProceso", citasEnProceso.size(),
                "diagnosticosGenerados", diagnosticosCache.size()
            ));
            ctx.contentType("application/json").result(jsonResponse);
        });

        System.out.println("\n=================================");
        System.out.println("🌐 Servidor web iniciado");
        System.out.println("=================================");
        System.out.println("📍 URL: http://localhost:7070");
        System.out.println("🔌 API: http://localhost:7070/api");
        System.out.println("=================================\n");
    }

    // Método auxiliar para simular el procesamiento de la cita
    // (mientras implementas la lógica completa en los agentes)
    private static void procesarCitaSimulada(Cita cita) {
        try {
            System.out.println("🔄 Iniciando procesamiento de cita para: " + cita.getPacienteId());

            // Simular tiempo de procesamiento (recepcionista -> enfermero -> doctor)
            Thread.sleep(3000); // 3 segundos de recepción
            System.out.println("📋 Recepcionista procesó la cita");

            Thread.sleep(2000); // 2 segundos enfermero
            System.out.println("💉 Enfermero tomó signos vitales");

            Thread.sleep(3000); // 3 segundos doctor
            System.out.println("👨‍⚕️ Doctor realizó diagnóstico");

            // Generar diagnóstico basado en síntomas
            Diagnostico diagnostico = generarDiagnostico(cita);

            // Guardar en cache
            diagnosticosCache.put(cita.getPacienteId(), diagnostico);

            System.out.println("✅ Diagnóstico completo para: " + cita.getPacienteId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Diagnostico generarDiagnostico(Cita cita) {
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setPacienteId(cita.getPacienteId());

        String sintomas = cita.getSintomas().toLowerCase();

        // Lógica simple de diagnóstico basada en síntomas
        if (sintomas.contains("fiebre") || sintomas.contains("gripe") || sintomas.contains("resfriado")) {
            diagnostico.setDiagnostico("Infección respiratoria aguda (Gripe común)");
            diagnostico.setTratamiento("Reposo, hidratación abundante, paracetamol 500mg cada 8 horas. Evitar exposición al frío.");
            diagnostico.setFechaProxima("Seguimiento en 7 días si los síntomas persisten");
        } else if (sintomas.contains("dolor de cabeza") || sintomas.contains("migraña")) {
            diagnostico.setDiagnostico("Cefalea tensional");
            diagnostico.setTratamiento("Ibuprofeno 400mg cada 8 horas, descanso adecuado, evitar estrés.");
            diagnostico.setFechaProxima("Control en 15 días");
        } else if (sintomas.contains("dolor de estómago") || sintomas.contains("gastritis") || sintomas.contains("náuseas")) {
            diagnostico.setDiagnostico("Gastritis aguda");
            diagnostico.setTratamiento("Omeprazol 20mg en ayunas, dieta blanda, evitar picantes y alcohol.");
            diagnostico.setFechaProxima("Control en 10 días");
        } else if (sintomas.contains("tos") || sintomas.contains("garganta")) {
            diagnostico.setDiagnostico("Faringitis aguda");
            diagnostico.setTratamiento("Amoxicilina 500mg cada 8 horas por 7 días, abundantes líquidos.");
            diagnostico.setFechaProxima("Control en 7 días");
        } else {
            diagnostico.setDiagnostico("Evaluación general - Síntomas no específicos");
            diagnostico.setTratamiento("Observación, mantener hidratación. Si los síntomas empeoran, acudir a urgencias.");
            diagnostico.setFechaProxima("Control en 5 días o antes si empeora");
        }

        return diagnostico;
    }

    // Método público para que los agentes puedan guardar diagnósticos
    public static void guardarDiagnostico(String pacienteId, Diagnostico diagnostico) {
        diagnosticosCache.put(pacienteId, diagnostico);
        System.out.println("✅ Diagnóstico guardado para paciente: " + pacienteId);
    }
}
