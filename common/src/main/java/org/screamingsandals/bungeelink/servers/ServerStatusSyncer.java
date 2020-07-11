package org.screamingsandals.bungeelink.servers;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bungeelink.ServerPlatform;
import org.screamingsandals.bungeelink.network.methods.ServerStatusMethod;

import java.util.*;

@RequiredArgsConstructor
public class ServerStatusSyncer {
    private final ServerPlatform platform;
    private final List<Server> registeredServers = Collections.synchronizedList(new ArrayList<>());
    private StreamObserver<ServerStatusMethod.ServerStatusRequest> statusObserver;

    public void register(Server server) {
        if (statusObserver == null) {
            startCall();
        }
        statusObserver.onNext(new ServerStatusMethod.ServerStatusRequest(ServerStatusMethod.RequestType.REGISTER, server.getServerName()));
        registeredServers.add(server);
    }

    private void startCall() {
        statusObserver = platform.getClient().initStreamCall(ServerStatusMethod.METHOD, new StreamObserver<>() {
            @Override
            public void onNext(ServerStatusMethod.ServerStatusResponse message) {
                Server receivedServer = platform.getServerManager().getServer(message.getName());
                receivedServer.setMotd(message.getMotd());
                receivedServer.setOnlinePlayersCount(message.getCurrentPlayerCount());
                receivedServer.setMaximumPlayersCount(message.getMaximumPlayerCount());
                receivedServer.setServerStatus(message.getServerStatus());
                receivedServer.getThirdPartyInformationHolder().switchMap(new HashMap<>(message.getThirdPartyInformation()));
                platform.getUpdateServerStatusDispatcher().fire(receivedServer);
            }

            @Override
            public void onError(Throwable t) {
                platform.getLogger().warning("Status Syncer Stream went down! But don't worry, we will try revive this connection after some time!");
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startCall();
                }).start();
            }

            @Override
            public void onCompleted() {
                statusObserver = null;
                registeredServers.clear();
            }
        });
        registeredServers.forEach(registeredServer -> statusObserver.onNext(new ServerStatusMethod.ServerStatusRequest(ServerStatusMethod.RequestType.REGISTER, registeredServer.getServerName())));
    }

    public void unregister(Server server) {
        if (statusObserver != null && registeredServers.contains(server)) {
            statusObserver.onNext(new ServerStatusMethod.ServerStatusRequest(ServerStatusMethod.RequestType.UNREGISTER, server.getServerName()));
            registeredServers.remove(server);
        }
    }

    public void shutdown() {
        if (statusObserver != null) {
            statusObserver.onCompleted();
        }
        registeredServers.clear();
    }

    public boolean areServerChangesHandled(Server server) {
        return registeredServers.contains(server) || server.getServerName().equals(Objects.requireNonNull(platform.getThisServerInformation()).getServerName());
    }

}
