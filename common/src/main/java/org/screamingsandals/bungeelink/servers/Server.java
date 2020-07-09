package org.screamingsandals.bungeelink.servers;

import io.grpc.stub.StreamObserver;
import lombok.Data;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.network.methods.CustomPayloadMethod;

@Data
public class Server implements org.screamingsandals.bungeelink.api.servers.Server {
    private final String serverName;
    private ServerStatus serverStatus = ServerStatus.CLOSED;
    private String statusLine = "A BungeeLink server";
    private int onlinePlayersCount;
    private int maximumPlayersCount;

    // Proxy platform only
    private String publicKey;
    private StreamObserver<CustomPayloadMethod.CustomPayloadMessage> customPayloadMessageStreamObserver;

    /*
     * PROXY PLATFORM ONLY!!!!!
     */
    public void provideCustomPayloadToServer(CustomPayloadMethod.SenderType senderType, String senderName, String channel, Object message) {
        if (customPayloadMessageStreamObserver != null) {
            CustomPayloadMethod.CustomPayloadMessage msg = new CustomPayloadMethod.CustomPayloadMessage();
            msg.setSenderType(senderType);
            msg.setSenderName(senderName);
            msg.setChannel(channel);
            msg.setReceiverType(CustomPayloadMethod.ReceiverType.SERVER);
            msg.setReceiverName(serverName);
            Platform.getInstance().fillWithPayload(msg, message);
            customPayloadMessageStreamObserver.onNext(msg);
        }
    }
}
