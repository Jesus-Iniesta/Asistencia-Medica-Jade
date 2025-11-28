package com.medical.jade.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class RegisterServiceBehaviour extends OneShotBehaviour {
    private String serviceType;
    private String serviceName;

    public RegisterServiceBehaviour(String serviceType, String serviceName) {
        this.serviceType = serviceType;
        this.serviceName = serviceName;
    }

    @Override
    public void action() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(myAgent.getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            sd.setName(serviceName);
            dfd.addServices(sd);

            DFService.register(myAgent, dfd);
            System.out.println("[" + myAgent.getLocalName() + "] Servicio registrado: " + serviceType);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}