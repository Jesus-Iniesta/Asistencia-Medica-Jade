package com.medical.jade.network;

/**
 * Simple DTO that travels over the raw TCP socket to mirror an ACL-like payload
 * between two independent JADE plataformas.
 */
public class RemoteMessageEnvelope {
    private String sender;
    private String receiver;
    private int performative;
    private String content;

    public RemoteMessageEnvelope() {
    }

    public RemoteMessageEnvelope(String sender, String receiver, int performative, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.performative = performative;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getPerformative() {
        return performative;
    }

    public void setPerformative(int performative) {
        this.performative = performative;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

