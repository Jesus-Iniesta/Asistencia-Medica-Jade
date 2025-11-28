package com.medical.jade.behaviours;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SearchServiceBehaviour extends OneShotBehaviour {
    private String serviceType;
    private ServiceFoundHandler handler;

    public interface ServiceFoundHandler {
        void onServiceFound(AID[] agents);
    }

    public SearchServiceBehaviour(String serviceType, ServiceFoundHandler handler) {
        this.serviceType = serviceType;
        this.handler = handler;
    }

    @Override
    public void action() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(myAgent, template);

            AID[] agents = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                agents[i] = result[i].getName();
            }

            handler.onServiceFound(agents);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}