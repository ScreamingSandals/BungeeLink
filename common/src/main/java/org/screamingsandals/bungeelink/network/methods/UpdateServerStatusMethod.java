package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.bungeelink.servers.ServerThirdPartyInformationHolder;

import java.util.HashMap;
import java.util.Map;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class UpdateServerStatusMethod {

    @Data
    public static class UpdateServerStatusRequest {
        ServerStatus serverStatus;
        String motd;
        int currentPlayersCount;
        int maximumPlayersCount;
        Map<String, String> thirdPartyInformation;
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
        server.setMotd(request.motd);
        server.setOnlinePlayersCount(request.currentPlayersCount);
        server.setMaximumPlayersCount(request.maximumPlayersCount);
        server.getThirdPartyInformationHolder().switchMap(new HashMap<>(request.thirdPartyInformation));
        responseObserver.onNext(new UpdateServerStatusResponse());
        responseObserver.onCompleted();

        Platform.getInstance().getUpdateServerStatusDispatcher().fire(server);
    }
}
