package org.screamingsandals.bungeelink.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bungeelink.api.servers.Server;

import java.net.SocketAddress;
import java.util.UUID;

/**
 * This interface offers you just current information. Make sure you don't work with old one.
 *
 * Better player API will be available soon!
 */
public interface CurrentPlayerInformation {
    @NotNull
    String getName();

    @NotNull
    UUID getUuid();

    @Nullable
    Server getCurrentServer();

    @Nullable
    Server getPendingServer();

    @NotNull SocketAddress getAddress();
}
