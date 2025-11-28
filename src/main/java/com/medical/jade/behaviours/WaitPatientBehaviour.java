package com.medical.jade.behaviours;

import jade.core.behaviours.TickerBehaviour;

public class WaitPatientBehaviour extends TickerBehaviour {
    private int waitTime;
    private PatientArrivalHandler handler;

    public interface PatientArrivalHandler {
        void onPatientArrival();
    }

    public WaitPatientBehaviour(jade.core.Agent a, long period, PatientArrivalHandler handler) {
        super(a, period);
        this.handler = handler;
    }

    @Override
    protected void onTick() {
        handler.onPatientArrival();
    }
}