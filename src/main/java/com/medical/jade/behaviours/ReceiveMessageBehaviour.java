package com.medical.jade.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveMessageBehaviour extends CyclicBehaviour {
    private MessageTemplate template;
    private MessageHandler handler;

    public interface MessageHandler {
        void handleMessage(ACLMessage msg);
    }

    public ReceiveMessageBehaviour(MessageTemplate template, MessageHandler handler) {
        this.template = template;
        this.handler = handler;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            handler.handleMessage(msg);
        } else {
            block();
        }
    }
}