package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.ProxyPlatform;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;
import org.screamingsandals.bungeelink.servers.Server;

import java.util.UUID;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class SendPlayerMethod {

    @Data
    public static class SendPlayerRequest {
        UUID playerUuid;
        String destination;
    }

    @Data
    public static class SendPlayerResponse {
    }

    public static final MethodDescriptor<SendPlayerRequest, SendPlayerResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(SendPlayerRequest.class),
                    marshallerFor(SendPlayerResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "SendPlayer"))
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setSampledToLocalTracing(true)
                    .build();

    public static void onRequest(SendPlayerRequest request, StreamObserver<SendPlayerResponse> responseObserver) {
        Server server = Platform.getInstance().getServerManager().getServer(request.getDestination());
        if (server == null) {
            responseObserver.onError(new IllegalStateException("Provided server is not valid!"));
            return;
        }
        ((ProxyPlatform) Platform.getInstance()).getSendPlayerToServer().accept(request.getPlayerUuid(), server);
        responseObserver.onNext(new SendPlayerResponse());
        responseObserver.onCompleted();
    }
}
