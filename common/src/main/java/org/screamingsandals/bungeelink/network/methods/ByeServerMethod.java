package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;
import org.screamingsandals.bungeelink.servers.Server;

import java.util.HashMap;
import java.util.List;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class ByeServerMethod {

    public static class ByeServerRequest {
    }

    public static class ByeServerResponse {
    }

    public static final MethodDescriptor<ByeServerRequest, ByeServerResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(ByeServerRequest.class),
                    marshallerFor(ByeServerResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "ByeServer"))
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setSampledToLocalTracing(true)
                    .build();

    public static void onRequest(ByeServerRequest request, StreamObserver<ByeServerResponse> responseObserver) {
        String token = Constants.CONTEXT_PUBLIC_TOKEN.get();
        Server server = Platform.getInstance().getServerManager().getServerByPublicKey(token);

        server.setOnlinePlayersCount(0);
        server.setMotd("A BungeeLink server");
        server.setServerStatus(ServerStatus.CLOSED);
        server.getThirdPartyInformationHolder().switchMap(new HashMap<>());

        responseObserver.onNext(new ByeServerResponse());
        responseObserver.onCompleted();
    }
}
