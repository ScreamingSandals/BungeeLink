package org.screamingsandals.bungeelink.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bungeelink.api.custom.CustomPayloadManager;
import org.screamingsandals.bungeelink.api.servers.Server;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class BungeeLinkAPI {
    @Nullable
    public abstract Server getThisServerInformation();

    public abstract void processUpdate();

    @Nullable
    public abstract Server getServerByName(@NotNull String serverName);

    public abstract void registerServerDataListener(@NotNull String serverName, @NotNull Consumer<Server> listener);

    public abstract void getPlayerServer(@NotNull UUID uuid, @NotNull Consumer<Server> consumer);

    public abstract void changePlayerServer(@NotNull UUID uuid, @NotNull Server server);

    @NotNull
    public abstract CustomPayloadManager getCustomPayloadManager();

    @NotNull
    public static BungeeLinkAPI getInstance() {
        return api;
    }

    protected static BungeeLinkAPI api;
}
