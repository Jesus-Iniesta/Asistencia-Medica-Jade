package com.medical.jade.behaviours;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendResponseBehaviour extends OneShotBehaviour {
    private AID receiver;
    private int performative;
    private String content;

    public SendResponseBehaviour(AID receiver, int performative, String content) {
        this.receiver = receiver;
        this.performative = performative;
        this.content = content;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(performative);
        msg.addReceiver(receiver);
        msg.setContent(content);
        myAgent.send(msg);

        System.out.println("[" + myAgent.getLocalName() + "] Mensaje enviado a: " + receiver.getLocalName());
    }
}