package com.medical.jade.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ProcessRequestBehaviour extends OneShotBehaviour {
    private ACLMessage request;
    private RequestProcessor processor;

    public interface RequestProcessor {
        void process(ACLMessage request);
    }

    public ProcessRequestBehaviour(ACLMessage request, RequestProcessor processor) {
        this.request = request;
        this.processor = processor;
    }

    @Override
    public void action() {
        processor.process(request);
    }
}