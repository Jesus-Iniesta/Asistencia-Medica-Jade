package com.medical.jade.behaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestPerformerBehaviour extends Behaviour {
    private final AID targetAgent;
    private final String requestContent;
    private int step = 0;
    private final ResponseHandler handler;
    private String replyWith;

    public interface ResponseHandler {
        void onResponse(ACLMessage response);
        void onFailure();
    }

    public RequestPerformerBehaviour(AID targetAgent, String requestContent, ResponseHandler handler) {
        this.targetAgent = targetAgent;
        this.requestContent = requestContent;
        this.handler = handler;
    }

    @Override
    public void action() {
        switch (step) {
            case 0:
                // Enviar solicitud
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(targetAgent);
                request.setContent(requestContent);
                request.setConversationId("request-" + System.currentTimeMillis());
                replyWith = "request" + System.currentTimeMillis();
                request.setReplyWith(replyWith);
                myAgent.send(request);
                step = 1;
                break;

            case 1:
                // Recibir respuesta
                MessageTemplate mt = MessageTemplate.MatchInReplyTo(replyWith);
                ACLMessage reply = myAgent.receive(mt);

                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        handler.onResponse(reply);
                    } else {
                        handler.onFailure();
                    }
                    step = 2;
                } else {
                    block();
                }
                break;
        }
    }

    @Override
    public boolean done() {
        return step == 2;
    }
}