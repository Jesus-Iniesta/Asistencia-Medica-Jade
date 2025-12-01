# 📚 Documentación Técnica - Sistema Médico con JADE

## 📋 Índice
1. [Arquitectura General](#arquitectura-general)
2. [Agentes del Sistema](#agentes-del-sistema)
3. [Flujo de Comunicación](#flujo-de-comunicación)
4. [Mensajes y Protocolos](#mensajes-y-protocolos)
5. [Behaviours Implementados](#behaviours-implementados)
6. [Código Detallado](#código-detallado)

---

## 🏗️ Arquitectura General

El sistema implementa una arquitectura distribuida basada en agentes JADE (Java Agent DEvelopment Framework) que simula un sistema de atención médica con múltiples agentes cooperativos.

### Componentes Principales

```
┌─────────────────────────────────────────────────────────────┐
│                    PLATAFORMA JADE                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  MainContainer│  │WebContainer  │  │RemoteContainer│    │
│  │              │  │              │  │              │     │
│  │ Recepcionista│  │   Servidor   │  │    Doctor    │     │
│  │  Enfermero   │  │     Web      │  │  NetworkBridge│    │
│  │ NetworkBridge│  │              │  │  (Cliente)    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
         ↕                    ↕                    ↕
    ┌─────────┐          ┌─────────┐         ┌─────────┐
    │  JADE   │          │   HTTP  │         │  JADE   │
    │Messages │          │   API   │         │Messages │
    └─────────┘          └─────────┘         └─────────┘
         ↘                                      ↙
           ──────────── TCP Socket 6200 ─────────
```

**Puente TCP entre plataformas:** `NetworkBridgeAgent` crea un túnel bidireccional mediante sockets puros (modo `SERVER` en la computadora principal y `CLIENT` en la remota). Los mensajes JADE que deban viajar entre plataformas se encapsulan en `RemoteMessageEnvelope`, se serializan a JSON (Gson) y se envían por el puerto configurable (6200 por defecto, sobreescribible con `-Dbridge.port=PUERTO`). Este puente evita depender de RMI y mantiene la interoperabilidad incluso si las plataformas están separadas por firewalls más restrictivos. Si tu máquina tiene adaptadores virtuales (VirtualBox, Docker), fuerza la IP real con `-Dmain.host=IP_REAL` o verifica manualmente con `ipconfig/ifconfig` antes de ejecutar.

---

## 👥 Agentes del Sistema

### 1. **RecepcionistaAgent** 📋

**Responsabilidad:** Recibir y gestionar el proceso completo de atención médica, desde el registro hasta el pago.

**Ubicación:** `MainContainer`

**Behaviours:**
- `RegisterServiceBehaviour` - Registra el servicio de "recepcion" en Yellow Pages
- `ReceiveMessageBehaviour` - Escucha mensajes entrantes
- `ProcessRequestBehaviour` - Procesa solicitudes de los pacientes

#### Código Clave:

```java
public class RecepcionistaAgent extends Agent {
    protected void setup() {
        System.out.println("👨‍💼 Recepcionista iniciado: " + getLocalName());
        
        // Registrar servicio
        addBehaviour(new RegisterServiceBehaviour(this, "recepcion"));
        
        // Escuchar mensajes
        addBehaviour(new ReceiveMessageBehaviour(this, this::procesarMensaje));
    }
    
    private void procesarMensaje(ACLMessage msg) {
        String contenido = msg.getContent();
        
        if (contenido.startsWith("SOLICITUD_CITA:")) {
            manejarSolicitudCita(msg);
        } else if (contenido.startsWith("DIAGNOSTICO_COMPLETO:")) {
            manejarDiagnostico(msg);
        }
    }
}
```

**Servicios Registrados:**
- Tipo: `recepcion`
- DF (Directory Facilitator): Yellow Pages de JADE

---

### 2. **EnfermeroAgent** 💉

**Responsabilidad:** Tomar signos vitales del paciente y enviarlos al doctor.

**Ubicación:** `MainContainer`

**Behaviours:**
- `RegisterServiceBehaviour` - Registra el servicio de "enfermero"
- `ReceiveMessageBehaviour` - Recibe solicitudes de signos vitales
- `SendResponseBehaviour` - Envía signos vitales al doctor

#### Código Clave:

```java
public class EnfermeroAgent extends Agent {
    protected void setup() {
        System.out.println("💉 Enfermero iniciado: " + getLocalName());
        
        addBehaviour(new RegisterServiceBehaviour(this, "enfermero"));
        addBehaviour(new ReceiveMessageBehaviour(this, this::tomarSignosVitales));
    }
    
    private void tomarSignosVitales(ACLMessage msg) {
        // Simular toma de signos vitales
        String signosVitales = String.format(
            "SIGNOS_VITALES:Temp=%.1f,Presion=%d/%d,Pulso=%d",
            36.5 + random.nextDouble(),
            120 + random.nextInt(20),
            80 + random.nextInt(10),
            70 + random.nextInt(30)
        );
        
        // Buscar doctor y enviar
        AID doctor = buscarServicio("doctor");
        enviarMensaje(doctor, signosVitales);
    }
}
```

---

### 3. **DoctorAgent** 👨‍⚕️

**Responsabilidad:** Analizar síntomas, generar diagnósticos y tratamientos mediante un **Sistema Experto basado en Reglas**.

**Ubicación:** `RemoteContainer` (puede ejecutarse en otra computadora)

**Behaviours:**
- `RegisterServiceBehaviour` - Registra el servicio de "doctor"
- `ReceiveMessageBehaviour` - Recibe casos médicos
- **Sistema Experto de Diagnóstico** - Base de conocimiento médico

#### 🧠 Base de Conocimiento (Knowledge Base)

El Doctor Agent implementa un **sistema experto médico** que utiliza una base de conocimiento con patrones de síntomas para generar diagnósticos precisos. Esta base de conocimiento actúa como la "experiencia médica" del agente.

**Arquitectura del Sistema Experto:**

```
┌─────────────────────────────────────────────────────────┐
│              DOCTOR AGENT - SISTEMA EXPERTO             │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │         BASE DE CONOCIMIENTO                     │  │
│  │  (Map<String, PatternDiagnostico>)               │  │
│  │                                                  │  │
│  │  • Respiratorias (Gripe, Faringitis, etc.)      │  │
│  │  • Gastrointestinales (Gastritis, Diarrea)      │  │
│  │  • Neurológicas (Cefalea, Migraña)              │  │
│  │  • Dermatológicas (Alergias, Infecciones)       │  │
│  │  • Generales (Diagnóstico por defecto)          │  │
│  └──────────────────────────────────────────────────┘  │
│                         ↓                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │         MOTOR DE INFERENCIA                      │  │
│  │  • Análisis de síntomas                          │  │
│  │  • Coincidencia de patrones                      │  │
│  │  • Evaluación de signos vitales                  │  │
│  │  • Selección de tratamiento                      │  │
│  └──────────────────────────────────────────────────┘  │
│                         ↓                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │         GENERACIÓN DE DIAGNÓSTICO                │  │
│  │  • Diagnóstico médico                            │  │
│  │  • Tratamiento personalizado                     │  │
│  │  • Recomendaciones                               │  │
│  │  • Próxima cita                                  │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

#### Código Clave:

```java
public class DoctorAgent extends Agent {
    // 🧠 BASE DE CONOCIMIENTO MÉDICO
    private Map<String, PatternDiagnostico> baseDiagnosticos;
    
    protected void setup() {
        System.out.println("👨‍⚕️ Doctor iniciado: " + getLocalName());
        
        // Inicializar base de conocimiento
        inicializarBaseDiagnosticos();
        
        addBehaviour(new RegisterServiceBehaviour(this, "doctor"));
        addBehaviour(new ReceiveMessageBehaviour(this, this::diagnosticar));
    }
    
    /**
     * 🔬 Inicializa la base de conocimiento médico
     * Cada patrón contiene:
     * - Palabras clave de síntomas
     * - Diagnóstico asociado
     * - Tratamiento recomendado
     * - Severidad
     */
    private void inicializarBaseDiagnosticos() {
        baseDiagnosticos = new HashMap<>();
        
        // PATRÓN 1: Enfermedades Respiratorias
        baseDiagnosticos.put("GRIPE", new PatternDiagnostico(
            new String[]{"fiebre", "tos", "dolor", "cuerpo", "gripe", "resfriado", "escalofríos"},
            "Gripe (Influenza)",
            "Paracetamol 500mg cada 8 horas + Reposo + Hidratación abundante",
            "3-5 días para reevaluación",
            "MODERADA"
        ));
        
        baseDiagnosticos.put("FARINGITIS", new PatternDiagnostico(
            new String[]{"garganta", "dolor al tragar", "faringitis", "amigdalitis"},
            "Faringitis aguda",
            "Ibuprofeno 400mg cada 8 horas + Gárgaras con agua tibia y sal",
            "Una semana si no mejora",
            "LEVE"
        ));
        
        baseDiagnosticos.put("BRONQUITIS", new PatternDiagnostico(
            new String[]{"tos", "pecho", "flema", "bronquitis", "expectoración"},
            "Bronquitis aguda",
            "Ambroxol 30mg cada 8 horas + Vapor de agua + Evitar irritantes",
            "5 días para control",
            "MODERADA"
        ));
        
        // PATRÓN 2: Enfermedades Gastrointestinales
        baseDiagnosticos.put("GASTRITIS", new PatternDiagnostico(
            new String[]{"estómago", "gastritis", "acidez", "ardor", "dolor estomacal"},
            "Gastritis aguda",
            "Omeprazol 20mg antes del desayuno + Dieta blanda + Evitar irritantes",
            "2 semanas para seguimiento",
            "MODERADA"
        ));
        
        baseDiagnosticos.put("DIARREA", new PatternDiagnostico(
            new String[]{"diarrea", "evacuaciones", "líquidas", "vómito", "náuseas"},
            "Gastroenteritis aguda",
            "Suero oral + Dieta astringente + Loperamida si es necesario",
            "48 horas si no mejora",
            "LEVE"
        ));
        
        // PATRÓN 3: Enfermedades Neurológicas
        baseDiagnosticos.put("CEFALEA", new PatternDiagnostico(
            new String[]{"cabeza", "dolor de cabeza", "cefalea", "mareo"},
            "Cefalea tensional",
            "Paracetamol 500mg cada 8 horas + Descanso + Evitar estrés",
            "Una semana si persiste",
            "LEVE"
        ));
        
        baseDiagnosticos.put("MIGRAÑA", new PatternDiagnostico(
            new String[]{"migraña", "jaqueca", "dolor intenso", "cabeza", "náuseas", "luz"},
            "Migraña",
            "Sumatriptán 50mg al inicio del dolor + Reposo en lugar oscuro + Hidratación",
            "2 semanas para ajuste de tratamiento",
            "MODERADA"
        ));
        
        // PATRÓN 4: Enfermedades Dermatológicas
        baseDiagnosticos.put("ALERGIA", new PatternDiagnostico(
            new String[]{"alergia", "picazón", "ronchas", "urticaria", "comezón"},
            "Reacción alérgica cutánea",
            "Loratadina 10mg cada 24 horas + Evitar alérgeno + Crema de hidrocortisona",
            "3 días si no mejora",
            "LEVE"
        ));
        
        System.out.println("✅ Base de conocimiento médico cargada: " + 
                         baseDiagnosticos.size() + " patrones diagnósticos");
    }
    
    /**
     * 🔍 Motor de inferencia - Analiza síntomas contra la base de conocimiento
     */
    private void diagnosticar(ACLMessage msg) {
        String sintomas = extraerSintomas(msg.getContent());
        String signosVitales = extraerSignosVitales(msg.getContent());
        
        System.out.println("🔬 Analizando síntomas: " + sintomas);
        
        // Sistema experto de diagnóstico
        Diagnostico diagnostico = analizarSintomas(sintomas, signosVitales);
        
        System.out.println("✅ Diagnóstico generado: " + diagnostico.getDiagnostico());
        
        // Enviar diagnóstico al recepcionista
        enviarDiagnostico(diagnostico);
        
        // También guardar en servidor web
        guardarEnServidor(diagnostico);
    }
    
    /**
     * 🧪 Análisis de síntomas usando la base de conocimiento
     * Implementa búsqueda de patrones y coincidencia de palabras clave
     */
    private Diagnostico analizarSintomas(String sintomas, String signos) {
        String sintomasLower = sintomas.toLowerCase();
        
        // Buscar coincidencia en la base de conocimiento
        for (Map.Entry<String, PatternDiagnostico> entry : baseDiagnosticos.entrySet()) {
            PatternDiagnostico pattern = entry.getValue();
            
            // Verificar si los síntomas coinciden con el patrón
            if (pattern.coincide(sintomasLower)) {
                System.out.println("🎯 Patrón encontrado: " + entry.getKey());
                return pattern.generarDiagnostico(sintomas, signos, currentPacienteId);
            }
        }
        
        // Si no hay coincidencia, usar diagnóstico genérico
        System.out.println("ℹ️ No se encontró patrón específico, usando diagnóstico general");
        return diagnosticoGenerico(sintomas, signos);
    }
    
    /**
     * 📋 Diagnóstico genérico cuando no hay coincidencia en la base de conocimiento
     */
    private Diagnostico diagnosticoGenerico(String sintomas, String signos) {
        Diagnostico diag = new Diagnostico();
        diag.setDiagnostico("Malestar general - Requiere evaluación adicional");
        diag.setTratamiento("Observación + Manejo sintomático + Regresar si empeora");
        diag.setFechaProxima("24-48 horas para reevaluación");
        diag.setDoctorNombre("Dr. Pedro Ramírez");
        diag.setDoctorEspecialidad("Medicina General");
        return diag;
    }
}

/**
 * 🧬 Clase que representa un patrón de diagnóstico en la base de conocimiento
 */
class PatternDiagnostico {
    private String[] palabrasClave;
    private String diagnostico;
    private String tratamiento;
    private String proximaCita;
    private String severidad;
    
    public PatternDiagnostico(String[] palabrasClave, String diagnostico, 
                             String tratamiento, String proximaCita, String severidad) {
        this.palabrasClave = palabrasClave;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.proximaCita = proximaCita;
        this.severidad = severidad;
    }
    
    /**
     * 🔎 Verifica si los síntomas coinciden con este patrón
     * Usa algoritmo de coincidencia de palabras clave
     */
    public boolean coincide(String sintomas) {
        int coincidencias = 0;
        
        for (String palabra : palabrasClave) {
            if (sintomas.contains(palabra.toLowerCase())) {
                coincidencias++;
            }
        }
        
        // Requiere al menos 1 coincidencia para activar el patrón
        return coincidencias > 0;
    }
    
    /**
     * 📝 Genera un objeto Diagnostico basado en este patrón
     */
    public Diagnostico generarDiagnostico(String sintomas, String signos, String pacienteId) {
        Diagnostico diag = new Diagnostico();
        diag.setPacienteId(pacienteId);
        diag.setDiagnostico(diagnostico);
        diag.setTratamiento(tratamiento);
        diag.setFechaProxima(proximaCita);
        diag.setDoctorNombre("Dr. Pedro Ramírez");
        diag.setDoctorEspecialidad("Medicina General");
        
        // Agregar severidad al diagnóstico
        if (severidad.equals("MODERADA") || severidad.equals("GRAVE")) {
            diag.setDiagnostico("⚠️ " + diagnostico);
        }
        
        return diag;
    }
}
```

#### 📊 Ejemplo de Flujo de Diagnóstico

```
1. Síntomas ingresados: "Tengo fiebre, tos y dolor de cuerpo"
   ↓
2. Motor de Inferencia analiza contra base de conocimiento:
   ✓ Patrón GRIPE: coincidencia con ["fiebre", "tos", "dolor", "cuerpo"]
   ✗ Patrón FARINGITIS: sin coincidencia
   ✗ Patrón GASTRITIS: sin coincidencia
   ↓
3. Patrón GRIPE seleccionado (mayor coincidencia)
   ↓
4. Diagnóstico generado:
   - Diagnóstico: "Gripe (Influenza)"
   - Tratamiento: "Paracetamol 500mg cada 8 horas + Reposo + Hidratación"
   - Próxima cita: "3-5 días para reevaluación"
   - Severidad: MODERADA
```

**Características del Sistema Experto:**

✅ **Basado en reglas** - Cada patrón tiene palabras clave específicas
✅ **Escalable** - Fácil agregar nuevos patrones diagnósticos
✅ **Flexible** - Permite coincidencias parciales
✅ **Jerárquico** - Diagnósticos por categoría médica
✅ **Fallback** - Diagnóstico genérico si no hay coincidencias

**Base de Conocimiento Actual:**
- 🫁 **Respiratorias**: Gripe, Faringitis, Bronquitis
- 🫃 **Gastrointestinales**: Gastritis, Gastroenteritis
- 🧠 **Neurológicas**: Cefalea, Migraña
- 🩹 **Dermatológicas**: Alergias cutáneas
- 📋 **General**: Diagnóstico por defecto

---

### 4. **NetworkBridgeAgent** 🌐

**Responsabilidad:** Encapsular y transportar mensajes ACL entre contenedores JADE que viven en equipos distintos, usando un socket TCP persistente.

**Ubicación:** Se despliega en ambos lados. `MainContainer` lo crea en modo `SERVER`; `RemoteContainer` lo inicia en modo `CLIENT` apuntando a la IP/puerto del servidor.

**Modos y parámetros:**
- `Mode.SERVER [puerto]`: abre un `ServerSocket` (6200 por defecto) y espera conexiones entrantes.
- `Mode.CLIENT host puerto`: intenta conectarse repetidamente al servidor (`retry` cada 3 segundos) hasta lograr el enlace.
- Metadatos obligatorios en los mensajes: `REMOTE_TARGET`, `REMOTE_PERFORMATIVE`, `REMOTE_SOURCE`.

**Ciclo de vida:**
- `ReceiveMessageBehaviour` filtra mensajes con ontología `REMOTE-FORWARD`, los empaca en `RemoteMessageEnvelope` y los encola.
- Un `sendLoop` y un `receiveLoop` corren en paralelo, garantizando escritura y lectura separadas del socket.
- Cada mensaje recibido se reconstruye como `ACLMessage`, se marca con `REMOTE_SOURCE=true` y se reinyecta en la plataforma destino, conservando `performative`, `sender`, `receiver` y `content`.
- El agente gestiona reconexiones automáticas, `TCP_NODELAY`, `BufferedReader/Writer` en UTF-8 y cierre ordenado en `takeDown()`.

**Integración con otros componentes:**
- `RemoteMessagingService` adjunta los parámetros remotos antes de enviar al bridge.
- `MainContainer` y `RemoteContainer` exponen la propiedad del puente (`bridge.port`) para coordinar múltiples entornos.
- Gracias a este puente, el doctor remoto puede ejecutar diagnósticos aunque la red impida conexiones RMI directas.

---

### 5. **PacienteAgent** 👤

**Responsabilidad:** Representar a un paciente en el sistema, enviar solicitudes de cita.

**Ubicación:** `WebContainer` (creado dinámicamente)

**Ciclo de Vida:** Creado cuando se registra una cita desde la web

#### Código Clave:

```java
public class PacienteAgent extends Agent {
    private String pacienteId;
    private String nombre;
    private String sintomas;
    
    protected void setup() {
        Object[] args = getArguments();
        this.pacienteId = (String) args[0];
        this.nombre = (String) args[1];
        this.sintomas = (String) args[2];
        
        System.out.println("👤 Paciente iniciado: " + nombre);
        
        // Enviar solicitud de cita inmediatamente
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                solicitarCita();
            }
        });
    }
    
    private void solicitarCita() {
        AID recepcionista = buscarServicio("recepcion");
        
        String mensaje = String.format(
            "SOLICITUD_CITA:ID=%s,Nombre=%s,Sintomas=%s",
            pacienteId, nombre, sintomas
        );
        
        enviarMensaje(recepcionista, mensaje, ACLMessage.REQUEST);
    }
}
```

---

## 🔄 Flujo de Comunicación

### Diagrama de Secuencia Completo

```
Usuario      WebServer    Paciente    Recepcionista    Enfermero    Doctor
  │              │            │              │              │          │
  │─Registra─────►│           │              │              │          │
  │   Cita       │           │              │              │          │
  │              │           │              │              │          │
  │              │──Crea────►│              │              │          │
  │              │         Agente           │              │          │
  │              │           │              │              │          │
  │              │           │──SOLICITUD──►│              │          │
  │              │           │    CITA      │              │          │
  │              │           │              │              │          │
  │              │           │              │──Solicita───►│          │
  │              │           │              │   Signos     │          │
  │              │           │              │              │          │
  │              │           │              │◄─Signos──────│          │
  │              │           │              │   Vitales    │          │
  │              │           │              │              │          │
  │              │           │              │──Envía──────────────────►│
  │              │           │              │   Caso       │          │
  │              │           │              │   Médico     │          │
  │              │           │              │              │          │
  │              │           │              │◄─────────DIAGNÓSTICO────│
  │              │           │              │              │          │
  │              │           │              │              │          │
  │◄─Polling────│◄──────────│◄─Notifica────│              │          │
  │  Diagnóstico│           │  Diagnóstico │              │          │
  │              │           │              │              │          │
  │──Confirma───►│           │              │              │          │
  │    Pago      │           │              │              │          │
  │              │           │              │              │          │
  │──Ver────────►│           │              │              │          │
  │   Receta     │           │              │              │          │
```

### Paso a Paso Detallado

#### **PASO 1: Registro del Paciente** 📋

```javascript
// Cliente Web → Servidor HTTP
POST /api/cita
{
  "nombre": "Juan Pérez",
  "pacienteId": "P1732836000123",
  "sintomas": "Dolor de cabeza y fiebre"
}
```

```java
// Servidor → JADE
AgentController paciente = container.createNewAgent(
    "Paciente-P1732836000123",
    "com.medical.jade.agents.PacienteAgent",
    new Object[]{pacienteId, nombre, sintomas}
);
paciente.start();
```

---

#### **PASO 2: Solicitud de Cita** 💬

```java
// PacienteAgent → RecepcionistaAgent
ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
msg.addReceiver(recepcionista);
msg.setContent("SOLICITUD_CITA:ID=P1732836000123,Nombre=Juan Pérez,Sintomas=Dolor de cabeza y fiebre");
msg.setConversationId("cita-" + System.currentTimeMillis());
send(msg);
```

---

#### **PASO 3: Toma de Signos Vitales** 🩺

```java
// RecepcionistaAgent → EnfermeroAgent
ACLMessage solicitud = new ACLMessage(ACLMessage.REQUEST);
solicitud.addReceiver(enfermero);
solicitud.setContent("TOMAR_SIGNOS:" + pacienteId);
send(solicitud);

// EnfermeroAgent → RecepcionistaAgent
ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
respuesta.addReceiver(recepcionista);
respuesta.setContent("SIGNOS_VITALES:Temp=37.2,Presion=120/80,Pulso=75");
send(respuesta);
```

---

#### **PASO 4: Consulta Médica** 👨‍⚕️

```java
// RecepcionistaAgent → DoctorAgent
String casoMedico = String.format(
    "CASO_MEDICO:ID=%s,Nombre=%s,Sintomas=%s,Signos=%s",
    pacienteId, nombre, sintomas, signosVitales
);

ACLMessage consulta = new ACLMessage(ACLMessage.REQUEST);
consulta.addReceiver(doctor);
consulta.setContent(casoMedico);
consulta.setReplyWith("consulta-" + System.currentTimeMillis());
send(consulta);
```

---

#### **PASO 5: Diagnóstico** 📊

```java
// DoctorAgent → RecepcionistaAgent
Diagnostico diag = new Diagnostico();
diag.setPacienteId(pacienteId);
diag.setDiagnostico("Cefalea tensional");
diag.setTratamiento("Paracetamol 500mg cada 8 horas");
diag.setFechaProxima("Una semana si persisten síntomas");
diag.setDoctorNombre("Dr. Pedro Ramírez");
diag.setDoctorEspecialidad("Medicina General");

ACLMessage resultado = new ACLMessage(ACLMessage.INFORM);
resultado.addReceiver(recepcionista);
resultado.setContent("DIAGNOSTICO_COMPLETO:" + gson.toJson(diag));
send(resultado);

// DoctorAgent → WebServer (HTTP)
HttpClient.post("http://localhost:7070/api/diagnostico", diag);
```

---

#### **PASO 6: Notificación al Usuario** 🔔

```java
// WebServer polling
GET /api/diagnostico/P1732836000123
→ { diagnostico: "Cefalea tensional", ... }
```

---

## 📨 Mensajes y Protocolos

### Tipos de Mensajes ACL

| Performativa | Uso | Ejemplo |
|-------------|-----|---------|
| `REQUEST` | Solicitar acción | Solicitud de cita, toma de signos |
| `INFORM` | Informar resultado | Envío de diagnóstico, signos vitales |
| `QUERY_IF` | Consultar estado | Verificar disponibilidad |
| `AGREE` | Aceptar solicitud | Confirmar recepción de cita |
| `REFUSE` | Rechazar solicitud | No disponible |

### Formato de Mensajes

```java
// Estructura general
String mensaje = "TIPO_MENSAJE:param1=valor1,param2=valor2,...";

// Ejemplos
"SOLICITUD_CITA:ID=P123,Nombre=Juan,Sintomas=Fiebre"
"SIGNOS_VITALES:Temp=37.2,Presion=120/80,Pulso=75"
"CASO_MEDICO:ID=P123,Sintomas=...,Signos=..."
"DIAGNOSTICO_COMPLETO:{json}"
```

---

## 🔧 Behaviours Implementados

### 1. **RegisterServiceBehaviour** (OneShotBehaviour)

**Propósito:** Registrar un agente en el Directory Facilitator (Yellow Pages)

```java
public class RegisterServiceBehaviour extends OneShotBehaviour {
    private String serviceType;
    
    public void action() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(myAgent.getAID());
        
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(myAgent.getLocalName());
        dfd.addServices(sd);
        
        try {
            DFService.register(myAgent, dfd);
            System.out.println("✅ Servicio registrado: " + serviceType);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
```

---

### 2. **ReceiveMessageBehaviour** (CyclicBehaviour)

**Propósito:** Escuchar mensajes entrantes continuamente

```java
public class ReceiveMessageBehaviour extends CyclicBehaviour {
    private MessageHandler handler;
    
    public void action() {
        ACLMessage msg = myAgent.receive();
        
        if (msg != null) {
            handler.handle(msg);
        } else {
            block(); // Esperar siguiente mensaje
        }
    }
}
```

---

### 3. **SearchServiceBehaviour** (OneShotBehaviour)

**Propósito:** Buscar un servicio en Yellow Pages

```java
public class SearchServiceBehaviour extends OneShotBehaviour {
    private String serviceType;
    private ServiceFoundHandler handler;
    
    public void action() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);
        
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            if (result.length > 0) {
                AID provider = result[0].getName();
                handler.onFound(provider);
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
```

---

### 4. **ProcessRequestBehaviour** (CyclicBehaviour)

**Propósito:** Procesar solicitudes complejas con lógica de negocio

```java
public class ProcessRequestBehaviour extends CyclicBehaviour {
    private RequestProcessor processor;
    
    public void action() {
        ACLMessage msg = myAgent.receive(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        );
        
        if (msg != null) {
            ACLMessage reply = msg.createReply();
            
            try {
                String result = processor.process(msg.getContent());
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(result);
            } catch (Exception e) {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("ERROR: " + e.getMessage());
            }
            
            myAgent.send(reply);
        } else {
            block();
        }
    }
}
```

---

## 🌐 Integración Web-JADE

### Comunicación Bidireccional

```java
// JADE → WEB (HTTP Client)
OkHttpClient client = new OkHttpClient();
RequestBody body = RequestBody.create(
    json, MediaType.parse("application/json")
);
Request request = new Request.Builder()
    .url("http://localhost:7070/api/diagnostico")
    .post(body)
    .build();
client.newCall(request).execute();
```

```java
// WEB → JADE (Creación dinámica de agentes)
AgentController agent = container.createNewAgent(
    nombreAgente,
    claseAgente,
    argumentos
);
agent.start();
```

---

## 📊 Estructuras de Datos

### Clase Cita

```java
public class Cita implements Serializable {
    private String pacienteId;
    private String nombre;
    private String sintomas;
    private LocalDateTime fecha;
    
    // Getters y Setters
}
```

### Clase Diagnostico

```java
public class Diagnostico implements Serializable {
    private String pacienteId;
    private String diagnostico;
    private String tratamiento;
    private String fechaProxima;
    private String doctorNombre;
    private String doctorEspecialidad;
    
    // Getters y Setters
}
```

### Clase HistoriaClinica

```java
public class HistoriaClinica implements Serializable {
    private String pacienteId;
    private String nombre;
    private int edad;
    private String sintomas;
    private String signosVitales;
    private LocalDateTime fechaConsulta;
    
    // Getters y Setters
}
```

---

## 🔐 Características Avanzadas

### 1. **Yellow Pages (Directory Facilitator)**
- Registro dinámico de servicios
- Búsqueda de agentes por tipo
- Actualización automática

### 2. **Contenedores Distribuidos**
- MainContainer: Servicios centrales
- RemoteContainer: Agentes distribuidos
- WebContainer: Integración web

### 3. **Ciclo de Vida de Agentes**
- Creación dinámica
- Inicialización con parámetros
- Destrucción automática (opcional)

### 4. **Manejo de Concurrencia**
- ConcurrentHashMap para cache
- Behaviours thread-safe
- Sincronización de mensajes

---

## 📈 Escalabilidad

El sistema está diseñado para escalar:

- ✅ **Múltiples doctores**: Agregar más `DoctorAgent`
- ✅ **Múltiples enfermeros**: Balanceo de carga
- ✅ **Múltiples pacientes**: Creación dinámica
- ✅ **Múltiples contenedores**: Distribución geográfica
- ✅ **Alta disponibilidad**: Redundancia de servicios

---

## 🎯 Conclusión

Este sistema demuestra:
- **Arquitectura multi-agente** robusta
- **Comunicación distribuida** eficiente
- **Integración web-JADE** transparente
- **Separación de responsabilidades** clara
- **Escalabilidad horizontal** y vertical

El uso de JADE permite una implementación limpia de sistemas distribuidos con comunicación basada en mensajes, descubrimiento de servicios y ejecución distribuida.
