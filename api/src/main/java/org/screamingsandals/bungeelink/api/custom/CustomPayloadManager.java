package org.screamingsandals.bungeelink.api.custom;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bungeelink.api.Proxy;

public interface CustomPayloadManager {

    void registerIncomingChannel(@NotNull String channel, @NotNull CustomPayloadListener listener);

    void unregisterIncomingListener(@NotNull CustomPayloadListener listener);

    @NotNull
    CustomPayloadObserver registerOutgoingChannel(@NotNull String channel);

    @NotNull
    Proxy getProxy();
}
