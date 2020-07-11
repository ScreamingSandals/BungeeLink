package org.screamingsandals.bungeelink.features.proxy;

import org.screamingsandals.bungeelink.servers.Server;

import java.util.UUID;

public interface GetPlayerServerFeature {
    Server getPlayerServer(UUID uuid);
}
