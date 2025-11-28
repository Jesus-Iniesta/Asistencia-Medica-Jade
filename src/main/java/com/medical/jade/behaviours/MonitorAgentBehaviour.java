package com.medical.jade.behaviours;

import jade.core.behaviours.TickerBehaviour;

public class MonitorAgentBehaviour extends TickerBehaviour {
    private int messageCount = 0;

    public MonitorAgentBehaviour(jade.core.Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        System.out.println("=================================");
        System.out.println("📊 Monitor de " + myAgent.getLocalName());
        System.out.println("Mensajes procesados: " + messageCount);
        System.out.println("Estado: ACTIVO");
        System.out.println("=================================");
    }

    public void incrementMessageCount() {
        messageCount++;
    }
}