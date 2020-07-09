package org.screamingsandals.bungeelink.custom;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.ServerPlatform;
import org.screamingsandals.bungeelink.api.Proxy;
import org.screamingsandals.bungeelink.api.custom.Contactable;
import org.screamingsandals.bungeelink.network.methods.CustomPayloadMethod;

import java.util.Objects;

@RequiredArgsConstructor
public class CustomPayloadClientSession {
    private final ServerPlatform platform;
    private StreamObserver<CustomPayloadMethod.CustomPayloadMessage> customPayloadObserver;

    public void init() {
        if (platform.getClient().isRunning()) {
            customPayloadObserver = platform.getClient().initStreamCall(CustomPayloadMethod.METHOD, new StreamObserver<>() {
                @Override
                public void onNext(CustomPayloadMethod.CustomPayloadMessage message) {
                    Object originalPayload = Platform.getInstance().getPayload(message);

                    platform.getCustomPayloadManager().receiveMessage(platform.resloveSender(message.getSenderType(), message.getSenderName()), message.getChannel(), originalPayload);
                }

                @Override
                public void onError(Throwable t) {
                    customPayloadObserver = null;
                    platform.getLogger().warning("Custom Payload Stream went down! But don't worry, we will try revive this connection after some time!");
                    new Thread(() -> {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        init();
                    }).start();
                    platform.sayHello((ok, s) -> {
                    }); // maybe proxy was restarted and we need new data
                }

                @Override
                public void onCompleted() {
                    customPayloadObserver = null;
                }
            });
        }
    }

    public void sendCustomPayload(String channel, Object message, Contactable contactable) {
        if (customPayloadObserver != null) {
            CustomPayloadMethod.CustomPayloadMessage msg = new CustomPayloadMethod.CustomPayloadMessage();
            msg.setSenderType(CustomPayloadMethod.SenderType.SERVER);
            msg.setSenderName(Objects.requireNonNull(platform.getThisServerInformation()).getServerName());
            msg.setChannel(channel);
            msg.setReceiverType(contactable instanceof Proxy ? CustomPayloadMethod.ReceiverType.PROXY : CustomPayloadMethod.ReceiverType.SERVER);
            msg.setReceiverName(contactable.getName());
            platform.fillWithPayload(msg, message);
            customPayloadObserver.onNext(msg);
        } // TODO add queue and delivery this message later
    }

    public void broadcastCustomPayload(String channel, Object message) {
        if (customPayloadObserver != null) {
            CustomPayloadMethod.CustomPayloadMessage msg = new CustomPayloadMethod.CustomPayloadMessage();
            msg.setSenderType(CustomPayloadMethod.SenderType.SERVER);
            msg.setSenderName(Objects.requireNonNull(platform.getThisServerInformation()).getServerName());
            msg.setChannel(channel);
            msg.setReceiverType(CustomPayloadMethod.ReceiverType.ALL);
            msg.setReceiverName("ALL");
            platform.fillWithPayload(msg, message);
            customPayloadObserver.onNext(msg);
        } // TODO add queue and delivery this message later
    }

    public void shutdown() {
        if (customPayloadObserver != null) {
            customPayloadObserver.onCompleted();
        }
    }
}
