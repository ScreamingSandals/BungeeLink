package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;

import java.util.UUID;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class KickPlayerMethod {

    @Data
    @AllArgsConstructor
    public static class KickPlayerRequest {
        UUID playerUuid;
        String reason;

        public KickPlayerRequest() {}
    }

    public static class KickPlayerResponse {
    }

    public static final MethodDescriptor<KickPlayerRequest, KickPlayerResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(KickPlayerRequest.class),
                    marshallerFor(KickPlayerResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "KickPlayer"))
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setSampledToLocalTracing(true)
                    .build();

    public static void onRequest(KickPlayerRequest request, StreamObserver<KickPlayerResponse> responseObserver) {
        Platform.getInstance().kickPlayerFromProxy(request.playerUuid, request.reason);
    }
}
