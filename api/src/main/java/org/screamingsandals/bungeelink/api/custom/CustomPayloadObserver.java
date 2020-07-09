package org.screamingsandals.bungeelink.api.custom;

public interface CustomPayloadObserver {

    void sendMesasge(Contactable receiver, Object message);

    void broadcastMessage(Object message);
}
