package org.screamingsandals.bungeelink.api.custom;

public interface CustomPayloadListener {

    void handleMessage(Contactable sender, Object message);
}
