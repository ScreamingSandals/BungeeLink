package org.screamingsandals.bungeelink;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bungeelink.api.Proxy;
import org.screamingsandals.bungeelink.api.custom.Contactable;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.custom.Handshake;
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
    private StreamObserver<CustomPayloadMethod.CustomPayloadMessage> customPayloadObserver;

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
                            .put("contents",  List.of("§7[BungeeLink]", "%name%", "%statusline%", "%online%/%max%"));
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
        if (customPayloadObserver != null) {
            CustomPayloadMethod.CustomPayloadMessage msg = new CustomPayloadMethod.CustomPayloadMessage();
            msg.setSenderType(CustomPayloadMethod.SenderType.SERVER);
            msg.setSenderName(Objects.requireNonNull(getThisServerInformation()).getServerName());
            msg.setChannel(channel);
            msg.setReceiverType(contactable instanceof Proxy ? CustomPayloadMethod.ReceiverType.PROXY : CustomPayloadMethod.ReceiverType.SERVER);
            msg.setReceiverName(contactable.getName());
            fillWithPayload(msg, message);
            customPayloadObserver.onNext(msg);
        } // TODO add queue and delivery this message later
    }

    @Override
    public void broadcastCustomPayload(String channel, Object message) {
        if (customPayloadObserver != null) {
            CustomPayloadMethod.CustomPayloadMessage msg = new CustomPayloadMethod.CustomPayloadMessage();
            msg.setSenderType(CustomPayloadMethod.SenderType.SERVER);
            msg.setSenderName(Objects.requireNonNull(getThisServerInformation()).getServerName());
            msg.setChannel(channel);
            msg.setReceiverType(CustomPayloadMethod.ReceiverType.ALL);
            msg.setReceiverName("ALL");
            fillWithPayload(msg, message);
            customPayloadObserver.onNext(msg);
        } // TODO add queue and delivery this message later
    }

    public String getPublicToken() {
        return getConfigAdapter().getString("publicToken");
    }

    public void sayHello(BiConsumer<String, Server> callback) {
        var call = getClient().initCall(HelloServerMethod.METHOD);
        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(HelloServerMethod.HelloServerResponse message) {
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
                callback.accept("OK", getThisServerInformation());
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                if (status != Status.OK) {
                    String message = "ERROR";
                    if (status.getCause() != null) {
                        message = status.getCause().getMessage();
                    }
                    callback.accept(message, null);
                }
            }
        }, new Metadata());
        call.sendMessage(new HelloServerMethod.HelloServerRequest());
        call.halfClose();
        call.request(1);
    }

    public void initCustomPayloadStream() {
        customPayloadObserver = getClient().initStreamCall(CustomPayloadMethod.METHOD, new StreamObserver<>() {
            @Override
            public void onNext(CustomPayloadMethod.CustomPayloadMessage message) {
                Object originalPayload = Platform.getInstance().getPayload(message);

                getCustomPayloadManager().receiveMessage(resloveSender(message.getSenderType(), message.getSenderName()), message.getChannel(), originalPayload);
            }

            @Override
            public void onError(Throwable t) {
                customPayloadObserver = null;
                getLogger().warning("Custom Payload Stream went down! But don't worry, we will try revive this connection after some time!");
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    initCustomPayloadStream();
                }).start();
                sayHello((ok, s) -> {}); // maybe proxy was restarted and we need new data
            }

            @Override
            public void onCompleted() {
                customPayloadObserver = null;
            }
        });
        sendCustomPayload("BungeeLink", new Handshake(), getCustomPayloadManager().getProxy());
    }

    public void selfUpdate() {
        var server = getServerManager().getServerByPublicKey(getPublicToken());

        getUpdateServerStatusDispatcher().fire(server);

        var call = getClient().initCall(UpdateServerStatusMethod.METHOD);
        var req = new UpdateServerStatusMethod.UpdateServerStatusRequest();
        req.setCurrentPlayersCount(server.getOnlinePlayersCount());
        req.setMaximumPlayersCount(server.getMaximumPlayersCount());
        req.setServerStatus(ServerStatus.OPEN);
        req.setStatusString(server.getStatusLine());
        call.start(new ClientCall.Listener<>() {
        }, new Metadata());
        call.sendMessage(req);
        call.halfClose();
        call.request(1);
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
    public void getPlayerServer(@NotNull UUID uuid, @NotNull Consumer<org.screamingsandals.bungeelink.api.servers.Server> consumer) {
        var call = getClient().initCall(GetPlayerServerMethod.METHOD);
        var req = new GetPlayerServerMethod.GetPlayerServerRequest();
        req.setPlayerUuid(uuid);
        call.start(new ClientCall.Listener<>() {
            @Override
            public void onMessage(GetPlayerServerMethod.GetPlayerServerResponse message) {
                consumer.accept(getServerManager().getServer(message.getServerName()));
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                if (status != Status.OK) {
                    consumer.accept(null);
                }
            }
        }, new Metadata());
        call.sendMessage(req);
        call.halfClose();
        call.request(1);
    }

    @Override
    public void changePlayerServer(@NotNull UUID uuid, org.screamingsandals.bungeelink.api.servers.@NotNull Server server) {
        var call = getClient().initCall(SendPlayerMethod.METHOD);
        var req = new SendPlayerMethod.SendPlayerRequest();
        req.setPlayerUuid(uuid);
        req.setDestination(server.getServerName());

        call.start(new ClientCall.Listener<>() {
        }, new Metadata());
        call.sendMessage(req);

        call.halfClose();
        call.request(1);
    }
}
