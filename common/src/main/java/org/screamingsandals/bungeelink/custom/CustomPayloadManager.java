package org.screamingsandals.bungeelink.custom;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bungeelink.api.Proxy;
import org.screamingsandals.bungeelink.api.custom.Contactable;
import org.screamingsandals.bungeelink.api.custom.CustomPayloadListener;

@Getter
public class CustomPayloadManager implements org.screamingsandals.bungeelink.api.custom.CustomPayloadManager {
    private ListMultimap<String, CustomPayloadListener> listeners = ArrayListMultimap.create();

    @Override
    public void registerIncomingChannel(@NotNull String channel, @NotNull CustomPayloadListener listener) {
        listeners.put(channel, listener);
    }

    @Override
    public void unregisterIncomingListener(@NotNull CustomPayloadListener listener) {
        listeners.entries().forEach(entry -> {
            if (entry.getValue() == listener) {
                listeners.remove(entry.getKey(), listener);
            }
        });
    }

    @Override
    public @NotNull CustomPayloadObserver registerOutgoingChannel(@NotNull String channel) {
        return new CustomPayloadObserver(channel);
    }

    /*
     * TODO: when Proxy will be used also for something else, read saved value (and maybe migrate it to another manager)
     */
    @Override
    public @NotNull Proxy getProxy() {
        return new Proxy() {
            @Override
            public boolean equals(Object another) {
                return another instanceof Proxy;
            }
        };
    }

    public void receiveMessage(Contactable sender, String channel, Object message) {
        if (listeners.containsKey(channel)) {
            listeners.get(channel).forEach(listener -> listener.handleMessage(sender, message));
        }
    }
}
