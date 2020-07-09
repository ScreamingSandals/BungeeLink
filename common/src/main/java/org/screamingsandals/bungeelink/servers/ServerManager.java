package org.screamingsandals.bungeelink.servers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerManager {
    private final List<Server> servers = new ArrayList<>();

    public void addServer(Server server) {
        if (!servers.contains(server)) {
            servers.add(server);
        }
    }

    public void removeServer(Server server) {
        servers.remove(server);
    }

    public Server getServer(String name) {
        for (Server server : servers) {
            if (server.getServerName().equals(name)) {
                return server;
            }
        }
        return null;
    }

    public List<String> getServerNames() {
        var names = new ArrayList<String>();
        for (var server : servers) {
            names.add(server.getServerName());
        }
        return names;
    }

    // Only proxy shit, won't work on servers
    public Server getServerByPublicKey(String publicKey) {
        for (Server server : servers) {
            if (publicKey.equals(server.getPublicKey())) {
                return server;
            }
        }
        return null;
    }

    public void forEach(Consumer<Server> action) {
        servers.forEach(action);
    }
}
