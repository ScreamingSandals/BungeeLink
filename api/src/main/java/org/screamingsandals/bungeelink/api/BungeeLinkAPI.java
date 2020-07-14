package org.screamingsandals.bungeelink.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bungeelink.api.custom.CustomPayloadManager;
import org.screamingsandals.bungeelink.api.servers.Server;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class BungeeLinkAPI {
    @Nullable
    public abstract Server getThisServerInformation();

    public abstract void processUpdate();

    @Nullable
    public abstract Server getServerByName(@NotNull String serverName);

    public abstract void registerServerDataListener(@NotNull String serverName, @NotNull Consumer<Server> listener);

    public Future<CurrentPlayerInformation> getPlayerCredentials(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            CountDownLatch latch = new CountDownLatch(1);
            var reference = new AtomicReference<CurrentPlayerInformation>();
            getPlayerCredentials(uuid, (credentials) -> {
                reference.set(credentials);
                latch.countDown();
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return reference.get();
        });
    }

    public abstract void getPlayerCredentials(@NotNull UUID uuid, @NotNull Consumer<CurrentPlayerInformation> consumer);

    public abstract void changePlayerServer(@NotNull UUID uuid, @NotNull Server server);

    public abstract void kickPlayerFromProxy(@NotNull UUID uuid, @Nullable String reason);

    @NotNull
    public abstract CustomPayloadManager getCustomPayloadManager();

    @NotNull
    public static BungeeLinkAPI getInstance() {
        return api;
    }

    protected static BungeeLinkAPI api;
}
