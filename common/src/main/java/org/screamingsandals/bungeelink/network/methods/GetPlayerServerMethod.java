package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;

import java.util.UUID;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class GetPlayerServerMethod {

    @Data
    @AllArgsConstructor
    public static class GetPlayerServerRequest {
        UUID playerUuid;

        public GetPlayerServerRequest() {}
    }

    @Data
    public static class GetPlayerServerResponse {
        String serverName;
    }

    public static final MethodDescriptor<GetPlayerServerRequest, GetPlayerServerResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(GetPlayerServerRequest.class),
                    marshallerFor(GetPlayerServerResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "VerifyPlayer"))
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setSampledToLocalTracing(true)
                    .build();

    public static void onRequest(GetPlayerServerRequest request, StreamObserver<GetPlayerServerResponse> responseObserver) {
        Platform.getInstance().getPlayerServer(request.getPlayerUuid(), server -> {
            GetPlayerServerResponse response = new GetPlayerServerResponse();
            if (server != null) {
                response.setServerName(server.getServerName());
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        });
    }
}
