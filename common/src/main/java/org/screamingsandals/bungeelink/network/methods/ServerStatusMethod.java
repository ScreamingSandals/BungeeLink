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
        String statusString;
        ServerStatus serverStatus = ServerStatus.CLOSED;
        int currentPlayerCount;
        int maximumPlayerCount;
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

    // TODO: remake this shit
    public static StreamObserver<ServerStatusRequest> onRequest(StreamObserver<ServerStatusResponse> responseObserver) {
        String whoAsked = Constants.CONTEXT_PUBLIC_TOKEN.get();

        return new StreamObserver<>() {
            @Override
            public void onNext(ServerStatusRequest request) {
                if (request.requestType == RequestType.STOP) {
                    responseObserver.onCompleted();
                    return;
                }

                Server server = Platform.getInstance().getServerManager().getServer(request.name);
                if (server == null) {
                    responseObserver.onError(new IllegalStateException("Server with name " + request.name + " doesn't exist!"));
                    return;
                }

                if (request.requestType == RequestType.REGISTER) {
                    ServerStatusResponse response = new ServerStatusResponse();
                    response.name = request.name;
                    response.statusString = server.getStatusLine();
                    response.serverStatus = server.getServerStatus();
                    response.currentPlayerCount = server.getOnlinePlayersCount();
                    response.maximumPlayerCount = server.getMaximumPlayersCount();
                    responseObserver.onNext(response);

                    var listener = new UpdateServerStatusListener() {
                        @Override
                        public void onUpdate(Server server) {
                            try {
                                ServerStatusResponse res = new ServerStatusResponse();
                                res.name = request.name;
                                res.statusString = server.getStatusLine();
                                res.serverStatus = server.getServerStatus();
                                res.currentPlayerCount = server.getOnlinePlayersCount();
                                res.maximumPlayerCount = server.getMaximumPlayersCount();
                                responseObserver.onNext(res);
                            } catch (Throwable ignored) {
                                // Unregister listener on fail
                                Platform.getInstance().getUpdateServerStatusDispatcher().unregister(server, this);
                                Platform.getInstance().getUpdateServerStatusDispatcher().unregister(whoAsked, this);
                            }
                        }
                    };

                    Platform.getInstance().getUpdateServerStatusDispatcher().register(server, listener);
                    Platform.getInstance().getUpdateServerStatusDispatcher().register(whoAsked, listener);
                } else {
                    var listeners = Platform.getInstance().getUpdateServerStatusDispatcher().getListenersByServer(server);
                    listeners.forEach(updateServerStatusListener -> {
                        if (Platform.getInstance().getUpdateServerStatusDispatcher().doesListenerBelongToToken(updateServerStatusListener, whoAsked)) {
                            Platform.getInstance().getUpdateServerStatusDispatcher().unregister(server, updateServerStatusListener);
                            Platform.getInstance().getUpdateServerStatusDispatcher().unregister(whoAsked, updateServerStatusListener);
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    public enum RequestType {
        REGISTER,
        UNREGISTER,
        STOP;
    }
}
