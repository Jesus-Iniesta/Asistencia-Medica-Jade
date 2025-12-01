package com.medical.jade.agents;

import com.google.gson.Gson;
import com.medical.jade.behaviours.ReceiveMessageBehaviour;
import com.medical.jade.network.RemoteMessageEnvelope;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * NetworkBridgeAgent crea un túnel TCP simple para transportar mensajes ACL
 * entre dos plataformas JADE independientes mediante sockets puros.
 */
public class NetworkBridgeAgent extends Agent {
    public static final String AGENT_NAME = "NetworkBridge";
    public static final String REMOTE_FORWARD_ONTOLOGY = "REMOTE-FORWARD";
    public static final String REMOTE_TARGET_PARAM = "REMOTE_TARGET";
    public static final String REMOTE_PERFORMATIVE_PARAM = "REMOTE_PERFORMATIVE";
    public static final String REMOTE_SOURCE_PARAM = "REMOTE_SOURCE";
    public static final int DEFAULT_PORT = 6200;
    private static final int RETRY_DELAY_MS = 3000;

    public enum Mode { SERVER, CLIENT }

    private Mode mode = Mode.SERVER;
    private String remoteHost;
    private int port = DEFAULT_PORT;
    private final Gson gson = new Gson();
    private final BlockingQueue<RemoteMessageEnvelope> outboundQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    @Override
    protected void setup() {
        parseArgs(getArguments());

        System.out.println("🌐 " + getLocalName() + " iniciado en modo " + mode + " puerto " + port);

        addBehaviour(new ReceiveMessageBehaviour(
                MessageTemplate.MatchOntology(REMOTE_FORWARD_ONTOLOGY),
                this::handleLocalForward
        ));

        executor.submit(this::sendLoop);
        executor.submit(this::receiveLoop);
    }

    private void parseArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        mode = Mode.valueOf(String.valueOf(args[0]).toUpperCase());

        if (mode == Mode.SERVER) {
            if (args.length >= 2) {
                port = Integer.parseInt(String.valueOf(args[1]));
            }
        } else {
            if (args.length < 2) {
                throw new IllegalArgumentException("Se requiere host remoto para modo CLIENT");
            }
            remoteHost = String.valueOf(args[1]);
            if (args.length >= 3) {
                port = Integer.parseInt(String.valueOf(args[2]));
            }
        }
    }

    private void handleLocalForward(ACLMessage msg) {
        String target = msg.getUserDefinedParameter(REMOTE_TARGET_PARAM);
        String performativeValue = msg.getUserDefinedParameter(REMOTE_PERFORMATIVE_PARAM);

        if (target == null || performativeValue == null) {
            System.err.println("⚠️  " + getLocalName() + " recibió mensaje sin metadatos remotos");
            return;
        }

        RemoteMessageEnvelope envelope = new RemoteMessageEnvelope(
                msg.getSender().getLocalName(),
                target,
                Integer.parseInt(performativeValue),
                msg.getContent()
        );

        outboundQueue.offer(envelope);
    }

    private void sendLoop() {
        while (running) {
            try {
                RemoteMessageEnvelope envelope = outboundQueue.take();
                ensureConnected();
                writer.write(gson.toJson(envelope));
                writer.write('\n');
                writer.flush();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                System.err.println("⚠️  Error enviando mensaje remoto: " + e.getMessage());
                closeSocket();
            }
        }
    }

    private void receiveLoop() {
        while (running) {
            try {
                ensureConnected();
                String line = reader.readLine();
                if (line == null) {
                    closeSocket();
                    continue;
                }

                RemoteMessageEnvelope envelope = gson.fromJson(line, RemoteMessageEnvelope.class);
                deliverRemoteEnvelope(envelope);
            } catch (IOException e) {
                if (running) {
                    System.err.println("⚠️  Error leyendo mensajes remotos: " + e.getMessage());
                }
                closeSocket();
            }
        }
    }

    private void deliverRemoteEnvelope(RemoteMessageEnvelope envelope) {
        ACLMessage msg = new ACLMessage(envelope.getPerformative());
        msg.addReceiver(new AID(envelope.getReceiver(), AID.ISLOCALNAME));
        msg.setSender(new AID(envelope.getSender(), AID.ISLOCALNAME));
        msg.setContent(envelope.getContent());
        msg.addUserDefinedParameter(REMOTE_SOURCE_PARAM, "true");
        send(msg);

        System.out.println("🔁 " + getLocalName() + " entregó mensaje de " + envelope.getSender() +
                " a " + envelope.getReceiver());
    }

    private synchronized void ensureConnected() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        closeSocket();

        if (mode == Mode.SERVER) {
            bindServer();
            System.out.println("🕓 " + getLocalName() + " esperando conexión en puerto " + port);
            socket = serverSocket.accept();
        } else {
            while (running) {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(remoteHost, port), 5000);
                    break;
                } catch (IOException e) {
                    System.err.println("⏳ No se pudo conectar con " + remoteHost + ":" + port +
                            ", reintentando en " + (RETRY_DELAY_MS / 1000) + "s");
                    sleepQuietly(RETRY_DELAY_MS);
                }
            }
        }

        if (socket == null) {
            throw new UncheckedIOException(new IOException("No se pudo establecer socket"));
        }

        socket.setTcpNoDelay(true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        System.out.println("🔗 " + getLocalName() + " enlazado con " + socket.getRemoteSocketAddress());
    }

    private void bindServer() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            return;
        }
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
    }

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        socket = null;
        reader = null;
        writer = null;
    }

    private void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
        serverSocket = null;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void takeDown() {
        running = false;
        executor.shutdownNow();
        closeSocket();
        closeServerSocket();
        System.out.println("🛑 " + getLocalName() + " detenido");
    }
}

