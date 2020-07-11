package org.screamingsandals.bungeelink.bukkit.hooks;

import org.screamingsandals.bungeelink.servers.Server;

public interface Hook {

    boolean isPossible();

    void hook(Server thisServer);
}
