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
import jade.wrapper.StaleProxyException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebInterfaceServer - Servidor web que se conecta a JADE
 * ⚠️ IMPORTANTE: Ejecuta MainContainer ANTES de este servidor
 */
public class WebInterfaceServer {
    private static final Gson gson = new Gson();
    private static final Map<String, Diagnostico> diagnosticosCache = new ConcurrentHashMap<>();
    private static ContainerController container;
    private static boolean jadeConnected = false;

    public static void main(String[] args) {
        System.out.println("\n===========================================");
        System.out.println("🔄 INICIANDO SERVIDOR WEB...");
        System.out.println("===========================================\n");

        // Intentar conectar a JADE
        try {
            System.out.println("🔌 Intentando conectar a plataforma JADE...");
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.CONTAINER_NAME, "web-container");

            container = rt.createAgentContainer(profile);

            // Verificar que el contenedor está funcional
            if (container != null) {
                // Intentar obtener el nombre del contenedor para verificar conexión
                String containerName = container.getContainerName();
                jadeConnected = true;
                System.out.println("✅ Conectado a plataforma JADE exitosamente");
                System.out.println("📦 Contenedor: " + containerName);
            }

        } catch (Exception e) {
            jadeConnected = false;
            System.err.println("\n❌ ERROR: No se pudo conectar a JADE");
            System.err.println("===========================================");
            System.err.println("⚠️  SOLUCIÓN:");
            System.err.println("   1. Primero ejecuta: MainContainer.java");
            System.err.println("   2. Espera a que aparezca la ventana de JADE");
            System.err.println("   3. Luego ejecuta: WebInterfaceServer.java");
            System.err.println("===========================================");
            System.err.println("Detalles del error: " + e.getMessage());
            System.err.println("\n⚠️  El servidor web se iniciará pero las funciones de JADE estarán deshabilitadas.\n");
        }

        // Servidor web (se inicia siempre, aunque JADE no esté disponible)
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
        System.out.println("🔗 JADE: " + (jadeConnected ? "✅ CONECTADO" : "❌ DESCONECTADO"));
        System.out.println("===========================================");

        if (jadeConnected) {
            System.out.println("\n💡 INSTRUCCIONES:");
            System.out.println("   1. Abre tu navegador");
            System.out.println("   2. Ve a: http://localhost:7070/index.html");
            System.out.println("   3. Llena el formulario de cita médica");
            System.out.println("   4. Observa la comunicación entre agentes");
        } else {
            System.out.println("\n⚠️  ADVERTENCIA:");
            System.out.println("   El servidor web está corriendo pero JADE no está conectado.");
            System.out.println("   Reinicia MainContainer y luego este servidor.");
        }
        System.out.println("===========================================\n");

        // API endpoints
        app.get("/", ctx -> {
            ctx.redirect("/index.html");
        });

        app.get("/api/health", ctx -> {
            String jsonResponse = gson.toJson(Map.of(
                "status", "ok",
                "server", "running",
                "jadeConnected", jadeConnected,
                "diagnosticos", diagnosticosCache.size()
            ));
            ctx.contentType("application/json").result(jsonResponse);
        });

        // Endpoint para recibir citas y crear agente paciente
        app.post("/api/cita", ctx -> {
            // Verificar conexión JADE
            if (!jadeConnected || container == null) {
                String errorResponse = gson.toJson(Map.of(
                    "status", "error",
                    "message", "⚠️ JADE no está conectado. Por favor, inicia MainContainer primero y reinicia este servidor."
                ));
                ctx.status(503).contentType("application/json").result(errorResponse);
                return;
            }

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

                // Crear diagnóstico inicial
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

            } catch (StaleProxyException e) {
                System.err.println("❌ Error: El contenedor JADE no está disponible");
                e.printStackTrace();
                String errorResponse = gson.toJson(Map.of(
                    "status", "error",
                    "message", "El contenedor JADE no está disponible. Por favor, reinicia MainContainer y luego este servidor."
                ));
                ctx.status(500).contentType("application/json").result(errorResponse);
            } catch (Exception e) {
                System.err.println("❌ Error al crear agente paciente");
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
