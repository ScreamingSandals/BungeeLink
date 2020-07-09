package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;
import org.screamingsandals.bungeelink.servers.Server;

import java.util.List;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class HelloServerMethod {

    @Data
    public static class HelloServerRequest {
    }

    @Data
    public static class HelloServerResponse {
        String serverName;

        List<String> servers;
    }

    public static final MethodDescriptor<HelloServerRequest, HelloServerResponse> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(HelloServerRequest.class),
                    marshallerFor(HelloServerResponse.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "HelloServer"))
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setSampledToLocalTracing(true)
                    .build();

    public static void onRequest(HelloServerRequest request, StreamObserver<HelloServerResponse> responseObserver) {
        String token = Constants.CONTEXT_PUBLIC_TOKEN.get();
        Server server = Platform.getInstance().getServerManager().getServerByPublicKey(token);
        var response = new HelloServerResponse();
        response.serverName = server.getServerName();
        response.servers = Platform.getInstance().getServerManager().getServerNames();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
