package com.medical.jade.network;

import com.medical.jade.agents.NetworkBridgeAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public final class RemoteMessagingService {
    private RemoteMessagingService() {
    }

    public static void sendRemote(Agent agent, String receiverLocalName, int performative, String content) {
        if (agent == null || receiverLocalName == null) {
            throw new IllegalArgumentException("Agent y receiver no pueden ser nulos");
        }
        ACLMessage bridgeMessage = new ACLMessage(ACLMessage.INFORM);
        bridgeMessage.addReceiver(new AID(NetworkBridgeAgent.AGENT_NAME, AID.ISLOCALNAME));
        bridgeMessage.setOntology(NetworkBridgeAgent.REMOTE_FORWARD_ONTOLOGY);
        bridgeMessage.addUserDefinedParameter(NetworkBridgeAgent.REMOTE_TARGET_PARAM, receiverLocalName);
        bridgeMessage.addUserDefinedParameter(NetworkBridgeAgent.REMOTE_PERFORMATIVE_PARAM, String.valueOf(performative));
        bridgeMessage.setContent(content);
        agent.send(bridgeMessage);
    }

    public static boolean isFromRemote(ACLMessage msg) {
        return msg != null && "true".equals(msg.getUserDefinedParameter(NetworkBridgeAgent.REMOTE_SOURCE_PARAM));
    }
}

