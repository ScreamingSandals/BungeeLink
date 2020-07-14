package org.screamingsandals.bungeelink;

import io.grpc.stub.StreamObserver;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bungeelink.api.CurrentPlayerInformation;
import org.screamingsandals.bungeelink.api.custom.Contactable;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.custom.CustomPayloadClientSession;
import org.screamingsandals.bungeelink.network.client.BungeeLinkClient;
import org.screamingsandals.bungeelink.network.methods.*;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.bungeelink.servers.ServerStatusSyncer;
import org.screamingsandals.bungeelink.utils.Encryption;
import org.screamingsandals.lib.config.DefaultConfigBuilder;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Getter
public class ServerPlatform extends Platform {
    private BungeeLinkClient client;
    private ServerStatusSyncer syncer = new ServerStatusSyncer(this);
    private CustomPayloadClientSession customPayloadClientSession = new CustomPayloadClientSession(this);

    @Override
    public boolean init() {
        if (!super.init()) {
            return false;
        }

        try {
            client = new BungeeLinkClient(InetAddress.getByName(getConfigAdapter().getString("server.ip")), getConfigAdapter().getInt("server.port"), getPublicToken(), getSecretToken(), getLogger());

            client.start();
        } catch (Exception e) {
            getLogger().severe("We couldn't initialize client! Check your configuration!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void loadDefaults() {
        DefaultConfigBuilder.start(getConfigAdapter())
                .put("enabled", false)
                .open("server", server -> {
                    server.put("ip", "127.0.0.1")
                            .put("port", 8082)
                            .put("secretToken", "PUT HERE TOKEN FROM BungeeLink PROXY CONFIGURATION");
                })
                .put("publicToken", Encryption.randomString(40))
                .open("sign", sign -> {
                    sign.put("enabled", true)
                            .put("contents",  List.of("§7[BungeeLink]", "%name%", "%motd%", "%online%/%max%"));
                })
                .put("verifyUsers", true)
                .end();

    }

    @Override
    public String getSecretToken() {
        return getConfigAdapter().getString("server.secretToken");
    }

    @Override
    public void sendCustomPayload(String channel, Object message, Contactable contactable) {
        customPayloadClientSession.sendCustomPayload(channel, message, contactable);
    }

    @Override
    public void broadcastCustomPayload(String channel, Object message) {
        customPayloadClientSession.broadcastCustomPayload(channel, message);
    }

    public String getPublicToken() {
        return getConfigAdapter().getString("publicToken");
    }

    /*
     * This method is not only for hello, but also for update
     */
    public void sayHello(BiConsumer<String, Server> callback) {
        getClient().initUnaryCall(HelloServerMethod.METHOD, new HelloServerMethod.HelloServerRequest(), new StreamObserver<>() {
            @Override
            public void onNext(HelloServerMethod.HelloServerResponse message) {
                if (getThisServerInformation() != null) {
                    if (!getThisServerInformation().getServerName().equals(message.getServerName())) {
                        getThisServerInformation().setPublicKey(null); // Whoa, we were changed to another server
                    }
                }
                message.getServers().forEach(name -> {
                    if (getServerManager().getServer(name) == null) {
                        var s = new Server(name);
                        getServerManager().addServer(s);
                    }
                    var s = getServerManager().getServer(name);
                    if (s.getServerName().equals(message.getServerName())) {
                        s.setPublicKey(getPublicToken());
                    }
                });
                getServerManager().forEach(server -> {
                    if (!message.getServers().contains(server.getServerName())) {
                        getServerManager().removeServer(server);
                    }
                });
                callback.accept("OK", getThisServerInformation());
            }

            @Override
            public void onError(Throwable t) {
                callback.accept(t.getMessage(), null);
            }

            @Override
            public void onCompleted() {
                // unused
            }
        });
    }

    public void sayBye() {
        getClient().initUnaryCall(ByeServerMethod.METHOD, new ByeServerMethod.ByeServerRequest());
    }

    public void selfUpdate() {
        var server = getServerManager().getServerByPublicKey(getPublicToken());

        getUpdateServerStatusDispatcher().fire(server);

        var req = new UpdateServerStatusMethod.UpdateServerStatusRequest();
        req.setCurrentPlayersCount(server.getOnlinePlayersCount());
        req.setMaximumPlayersCount(server.getMaximumPlayersCount());
        req.setServerStatus(ServerStatus.OPEN);
        req.setMotd(server.getMotd());
        req.setThirdPartyInformation(server.getThirdPartyInformationHolder().toMap());

        getClient().initUnaryCall(UpdateServerStatusMethod.METHOD, req);
    }

    @Override
    public void processUpdate() {
        this.selfUpdate();
    }

    @Override
    public @Nullable Server getThisServerInformation() {
        return getServerManager().getServerByPublicKey(getPublicToken());
    }

    @Override
    public void registerServerDataListener(@NotNull String serverName, @NotNull Consumer<org.screamingsandals.bungeelink.api.servers.Server> listener) {
        super.registerServerDataListener(serverName, listener);

        Server server = getServerManager().getServer(serverName);
        if (server == null) {
            return;
        }

        if (server.getServerName().equals(Objects.requireNonNull(getThisServerInformation()).getServerName())) {
            return;
        }

        if (!syncer.areServerChangesHandled(server)) {
            syncer.register(server);
        }
    }

    @Override
    public void getPlayerCredentials(@NotNull UUID uuid, @NotNull Consumer<CurrentPlayerInformation> consumer) {
        getClient().initUnaryCall(GetPlayerCredentialsMethod.METHOD, new GetPlayerCredentialsMethod.GetPlayerCredentialsRequest(uuid), new StreamObserver<>() {
            @Override
            public void onNext(GetPlayerCredentialsMethod.GetPlayerCredentialsResponse message) {
                PlayerInformation information = new PlayerInformation(message.getPlayerName(), uuid);
                information.setAddress(message.getAddress());
                information.setCurrentServer(getServerByName(message.getServerName()));
                information.setPendingServer(getServerByName(message.getPendingServerName()));
                consumer.accept(information);
            }

            @Override
            public void onError(Throwable t) {

                t.printStackTrace();
                consumer.accept(null);
            }

            @Override
            public void onCompleted() {
                // unused
            }
        });
    }

    @Override
    public void changePlayerServer(@NotNull UUID uuid, org.screamingsandals.bungeelink.api.servers.@NotNull Server server) {
        getClient().initUnaryCall(SendPlayerMethod.METHOD, new SendPlayerMethod.SendPlayerRequest(uuid, server.getServerName()));
    }

    @Override
    public void kickPlayerFromProxy(@NotNull UUID uuid, @Nullable String reason) {
        getClient().initUnaryCall(KickPlayerMethod.METHOD, new KickPlayerMethod.KickPlayerRequest(uuid, reason));
    }
}
