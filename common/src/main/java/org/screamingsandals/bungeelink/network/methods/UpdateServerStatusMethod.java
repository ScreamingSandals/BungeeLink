package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;
import org.screamingsandals.bungeelink.servers.Server;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class UpdateServerStatusMethod {

    @Data
    public static class UpdateServerStatusRequest {
        ServerStatus serverStatus;
        String statusString;
        int currentPlayersCount;
        int maximumPlayersCount;
    }

    public static class UpdateServerStatusResponse {
    }

    public static final MethodDescriptor<UpdateServerStatusRequest, UpdateServerStatusResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(UpdateServerStatusRequest.class),
                    marshallerFor(UpdateServerStatusResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "UpdateServerStatus"))
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setSampledToLocalTracing(true)
                    .build();

    public static void onRequest(UpdateServerStatusRequest request, StreamObserver<UpdateServerStatusResponse> responseObserver) {
        String token = Constants.CONTEXT_PUBLIC_TOKEN.get();
        Server server = Platform.getInstance().getServerManager().getServerByPublicKey(token);
        server.setServerStatus(request.serverStatus);
        server.setStatusLine(request.statusString);
        server.setOnlinePlayersCount(request.currentPlayersCount);
        server.setMaximumPlayersCount(request.maximumPlayersCount);
        responseObserver.onNext(new UpdateServerStatusResponse());
        responseObserver.onCompleted();

        Platform.getInstance().getUpdateServerStatusDispatcher().fire(server);
    }
}
