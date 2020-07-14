package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class GetPlayerCredentialsMethod {

    @Data
    @AllArgsConstructor
    public static class GetPlayerCredentialsRequest {
        UUID playerUuid;

        public GetPlayerCredentialsRequest() {}
    }

    @Data
    public static class GetPlayerCredentialsResponse {
        String playerName;
        String serverName;
        String pendingServerName;
        InetSocketAddress address;
    }

    public static final MethodDescriptor<GetPlayerCredentialsRequest, GetPlayerCredentialsResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(GetPlayerCredentialsRequest.class),
                    marshallerFor(GetPlayerCredentialsResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "GetPlayerServer"))
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setSampledToLocalTracing(true)
                    .build();

    public static void onRequest(GetPlayerCredentialsRequest request, StreamObserver<GetPlayerCredentialsResponse> responseObserver) {
        Platform.getInstance().getPlayerCredentials(request.getPlayerUuid(), credentials -> {
            GetPlayerCredentialsResponse response = new GetPlayerCredentialsResponse();
            if (credentials != null) {
                response.setPlayerName(credentials.getName());
                response.setAddress(credentials.getAddress() instanceof InetSocketAddress ? (InetSocketAddress) credentials.getAddress() : null);
                if (credentials.getCurrentServer() != null) {
                    response.setServerName(credentials.getCurrentServer().getServerName());
                }
                if (credentials.getPendingServer() != null) {
                    response.setPendingServerName(credentials.getPendingServer().getServerName());
                }
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        });
    }
}
