package org.screamingsandals.bungeelink.network.methods;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.network.server.BungeeLinkService;
import org.screamingsandals.bungeelink.servers.Server;

import static org.screamingsandals.bungeelink.network.MarshallerUtil.marshallerFor;

public class CustomPayloadMethod {

    @Data
    public static class CustomPayloadMessage {
        SenderType senderType;
        String senderName;
        String channel;
        ReceiverType receiverType;
        String receiverName;
        String payloadClassName;
        String payload;
    }
    public static final MethodDescriptor<CustomPayloadMessage, CustomPayloadMessage> METHOD =
            MethodDescriptor.newBuilder(
                    marshallerFor(CustomPayloadMessage.class),
                    marshallerFor(CustomPayloadMessage.class))
                    .setFullMethodName(
                            MethodDescriptor.generateFullMethodName(BungeeLinkService.SERVICE_NAME, "CustomPayload"))
                    .setType(MethodDescriptor.MethodType.BIDI_STREAMING)
                    .setSampledToLocalTracing(true)
                    .build();

    public static StreamObserver<CustomPayloadMessage> onRequest(StreamObserver<CustomPayloadMessage> responseObserver) {
        String whoAsked = Constants.CONTEXT_PUBLIC_TOKEN.get();

        // update observer
        Server server = Platform.getInstance().getServerManager().getServerByPublicKey(whoAsked);
        server.setCustomPayloadMessageStreamObserver(responseObserver);

        return new StreamObserver<>() {
            @Override
            public void onNext(CustomPayloadMessage request) {
                Object originalPayload = Platform.getInstance().getPayload(request);
                if (request.getReceiverType() == ReceiverType.PROXY) {
                    Platform.getInstance().getCustomPayloadManager().receiveMessage(Platform.getInstance().resloveSender(request.getSenderType(), request.getSenderName()), request.getChannel(), originalPayload);
                } else if (request.getReceiverType() == ReceiverType.SERVER) {
                    Server receiver = Platform.getInstance().getServerManager().getServer(request.getReceiverName());
                    if (receiver != null) {
                        receiver.provideCustomPayloadToServer(request.getSenderType(), request.getSenderName(), request.getChannel(), originalPayload);
                    }
                } else {
                    Platform.getInstance().getCustomPayloadManager().receiveMessage(Platform.getInstance().resloveSender(request.getSenderType(), request.getSenderName()), request.getChannel(), originalPayload);
                    Platform.getInstance().getServerManager().forEach(server1 -> server1.provideCustomPayloadToServer(request.getSenderType(), request.getSenderName(), request.getChannel(), originalPayload));
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

    public enum ReceiverType {
        PROXY,
        SERVER,
        ALL;
    }

    public enum SenderType {
        PROXY,
        SERVER;
    }
}
