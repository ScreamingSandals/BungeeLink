package org.screamingsandals.bungeelink.features.proxy;

import org.screamingsandals.bungeelink.api.CurrentPlayerInformation;

import java.util.UUID;

public interface GetPlayerCredentialsFeature {
    CurrentPlayerInformation getPlayerCredentials(UUID uuid);
}
