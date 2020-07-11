package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.event.UpdateServerStatusListener;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;
import org.screamingsandals.bungeelink.servers.Server;

import java.util.ArrayList;
import java.util.Map;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class ServerStatusMethod {

    @Data
    @AllArgsConstructor
    public static class ServerStatusRequest {
        RequestType requestType;
        String name;

        public ServerStatusRequest() {}
    }

    @Data
    public static class ServerStatusResponse {
        String name;
        String motd;
        ServerStatus serverStatus = ServerStatus.CLOSED;
        int currentPlayerCount;
        int maximumPlayerCount;
        Map<String, String> thirdPartyInformation;
    }

    public static final MethodDescriptor<ServerStatusRequest, ServerStatusResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(ServerStatusRequest.class),
                    marshallerFor(ServerStatusResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "ServerStatus"))
                    .setType(MethodDescriptor.MethodType.BIDI_STREAMING)
                    .setSampledToLocalTracing(true)
                    .build();

    public static StreamObserver<ServerStatusRequest> onRequest(StreamObserver<ServerStatusResponse> responseObserver) {
        String whoAsked = Constants.CONTEXT_PUBLIC_TOKEN.get();

        var servers = new ArrayList<Server>();

        var listener = new UpdateServerStatusListener() {
            @Override
            public void onUpdate(Server server) {
                if (servers.contains(server)) {
                    try {
                        ServerStatusResponse res = new ServerStatusResponse();
                        res.name = server.getServerName();
                        res.motd = server.getMotd();
                        res.thirdPartyInformation = server.getThirdPartyInformationHolder().toMap();
                        res.serverStatus = server.getServerStatus();
                        res.currentPlayerCount = server.getOnlinePlayersCount();
                        res.maximumPlayerCount = server.getMaximumPlayersCount();
                        responseObserver.onNext(res);
                    } catch (Throwable ignored) {
                        // Unregister listener on fail
                        Platform.getInstance().getUpdateServerStatusDispatcher().unregister(this);
                        Platform.getInstance().getUpdateServerStatusDispatcher().unregister(whoAsked,this);
                    }
                }
            }
        };

        Platform.getInstance().getUpdateServerStatusDispatcher().register(listener);

        return new StreamObserver<>() {
            @Override
            public void onNext(ServerStatusRequest request) {
                Server server = Platform.getInstance().getServerManager().getServer(request.name);
                if (server == null) {
                    responseObserver.onError(new IllegalStateException("Server with name " + request.name + " doesn't exist!"));
                    return;
                }

                if (request.requestType == RequestType.REGISTER) {
                    if (!servers.contains(server)) {
                        servers.add(server);

                        ServerStatusResponse res = new ServerStatusResponse();
                        res.name = server.getServerName();
                        res.motd = server.getMotd();
                        res.thirdPartyInformation = server.getThirdPartyInformationHolder().toMap();
                        res.serverStatus = server.getServerStatus();
                        res.currentPlayerCount = server.getOnlinePlayersCount();
                        res.maximumPlayerCount = server.getMaximumPlayersCount();
                        responseObserver.onNext(res);
                    }
                } else {
                    servers.remove(server);
                }
            }

            @Override
            public void onError(Throwable t) {
                Platform.getInstance().getUpdateServerStatusDispatcher().unregister(listener);
                Platform.getInstance().getUpdateServerStatusDispatcher().unregister(whoAsked, listener);
            }

            @Override
            public void onCompleted() {
                Platform.getInstance().getUpdateServerStatusDispatcher().unregister(listener);
                Platform.getInstance().getUpdateServerStatusDispatcher().unregister(whoAsked, listener);
            }
        };
    }

    public enum RequestType {
        REGISTER,
        UNREGISTER;
    }
}
