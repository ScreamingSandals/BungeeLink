package org.screamingsandals.bungeelink.features.proxy;

import java.util.UUID;

public interface KickPlayerFeature {
    void kickPlayer(UUID uuid, String reason);
}
