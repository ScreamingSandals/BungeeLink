package org.screamingsandals.bungeelink.custom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.api.custom.Contactable;

@RequiredArgsConstructor
@Getter
public class CustomPayloadObserver implements org.screamingsandals.bungeelink.api.custom.CustomPayloadObserver {
    private final String channel;

    @Override
    public void sendMesasge(Contactable contactable, Object message) {
        Platform.getInstance().sendCustomPayload(channel, message, contactable);
    }

    @Override
    public void broadcastMessage(Object message) {
        Platform.getInstance().broadcastCustomPayload(channel, message);
    }
}
