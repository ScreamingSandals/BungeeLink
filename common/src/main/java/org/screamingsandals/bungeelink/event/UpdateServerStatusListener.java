package org.screamingsandals.bungeelink.event;

import org.screamingsandals.bungeelink.servers.Server;

public interface UpdateServerStatusListener {
    void onUpdate(Server server);
}
