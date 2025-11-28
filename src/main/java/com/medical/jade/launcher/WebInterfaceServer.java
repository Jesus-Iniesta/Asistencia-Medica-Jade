package com.medical.jade.launcher;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import com.google.gson.Gson;
import com.medical.jade.messages.Cita;
import com.medical.jade.messages.Diagnostico;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebInterfaceServer - Servidor web que se conecta a JADE
 * Se usa DESPUÉS de iniciar MainContainer
 */
public class WebInterfaceServer {
    private static final Gson gson = new Gson();
    private static final Map<String, Diagnostico> diagnosticosCache = new ConcurrentHashMap<>();
    private static ContainerController container;

    public static void main(String[] args) {
        try {
            // Conectar a JADE existente
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.CONTAINER_NAME, "web-container");

            container = rt.createAgentContainer(profile);

            System.out.println("✅ Conectado a plataforma JADE");

        } catch (Exception e) {
            System.err.println("❌ Error conectando a JADE. Asegúrate de que MainContainer esté ejecutándose.");
            e.printStackTrace();
            return;
        }

        // Servidor web
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/webapp", Location.CLASSPATH);
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        }).start(7070);

        System.out.println("\n===========================================");
        System.out.println("🌐 INTERFAZ WEB - INICIADA");
        System.out.println("===========================================");
        System.out.println("📍 URL: http://localhost:7070");
        System.out.println("📄 Interfaz: http://localhost:7070/index.html");
        System.out.println("🔌 API: http://localhost:7070/api");
        System.out.println("===========================================");
        System.out.println("\n💡 INSTRUCCIONES:");
        System.out.println("   1. Abre tu navegador");
        System.out.println("   2. Ve a: http://localhost:7070/index.html");
        System.out.println("   3. Llena el formulario de cita médica");
        System.out.println("   4. Observa la comunicación entre agentes");
        System.out.println("===========================================\n");

        // API endpoints
        app.get("/", ctx -> {
            ctx.redirect("/index.html");
        });

        app.get("/api/health", ctx -> {
            String jsonResponse = gson.toJson(Map.of(
                "status", "ok",
                "server", "running",
                "jadeConnected", container != null,
                "diagnosticos", diagnosticosCache.size()
            ));
            ctx.contentType("application/json").result(jsonResponse);
        });

        // Endpoint para recibir citas y crear agente paciente
        app.post("/api/cita", ctx -> {
            try {
                String body = ctx.body();
                Cita cita = gson.fromJson(body, Cita.class);

                System.out.println("📋 Cita recibida desde web: " + body);
                System.out.println("👤 Creando agente paciente para: " + cita.getNombre());

                // Crear agente paciente dinámicamente
                String pacienteNombre = "Paciente-" + cita.getPacienteId();

                AgentController paciente = container.createNewAgent(
                    pacienteNombre,
                    "com.medical.jade.agents.PacienteAgent",
                    new Object[]{cita.getPacienteId(), cita.getNombre(), cita.getSintomas()}
                );

                paciente.start();

                System.out.println("✅ Agente " + pacienteNombre + " creado y enviando solicitud");

                // 🔥 CORREGIDO: Crear diagnóstico inicial en lugar de null
                Diagnostico diagnosticoInicial = new Diagnostico();
                diagnosticoInicial.setPacienteId(cita.getPacienteId());
                diagnosticoInicial.setDiagnostico("En proceso...");
                diagnosticoInicial.setTratamiento("Pendiente");
                diagnosticoInicial.setFechaProxima("Por determinar");
                diagnosticosCache.put(cita.getPacienteId(), diagnosticoInicial);

                String jsonResponse = gson.toJson(Map.of(
                    "status", "success",
                    "message", "Cita registrada. Agente paciente creado.",
                    "pacienteId", cita.getPacienteId()
                ));

                ctx.contentType("application/json").result(jsonResponse);

            } catch (Exception e) {
                e.printStackTrace();
                String errorResponse = gson.toJson(Map.of(
                    "status", "error",
                    "message", "Error al crear agente paciente: " + e.getMessage()
                ));
                ctx.status(500).contentType("application/json").result(errorResponse);
            }
        });

        // Endpoint para obtener diagnóstico
        app.get("/api/diagnostico/{pacienteId}", ctx -> {
            String pacienteId = ctx.pathParam("pacienteId");

            Diagnostico diagnostico = diagnosticosCache.get(pacienteId);

            if (diagnostico != null) {
                String jsonResponse = gson.toJson(diagnostico);
                ctx.contentType("application/json").result(jsonResponse);
            } else {
                String jsonResponse = gson.toJson(Map.of(
                    "status", "pending",
                    "message", "Diagnóstico en proceso..."
                ));
                ctx.contentType("application/json").result(jsonResponse);
            }
        });

        // Endpoint para que agentes guarden diagnósticos
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
    }
}
