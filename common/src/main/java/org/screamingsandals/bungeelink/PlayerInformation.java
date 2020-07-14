package org.screamingsandals.bungeelink;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bungeelink.api.CurrentPlayerInformation;
import org.screamingsandals.bungeelink.servers.Server;

import java.net.SocketAddress;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class PlayerInformation implements CurrentPlayerInformation {
    private final String name;
    private final UUID uuid;
    private Server currentServer;
    private Server pendingServer;
    private SocketAddress address;
}
