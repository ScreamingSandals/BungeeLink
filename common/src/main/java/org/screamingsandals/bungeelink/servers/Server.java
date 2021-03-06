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
    private String motd = "A BungeeLink server";
    private ServerThirdPartyInformationHolder thirdPartyInformationHolder = new ServerThirdPartyInformationHolder();
    private int onlinePlayersCount;
    private int maximumPlayersCount;

    // Proxy platform only
    private String publicKey;
    private StreamObserver<CustomPayloadMethod.CustomPayloadMessage> customPayloadMessageStreamObserver;

    /*
     * PROXY PLATFORM ONLY!!!!!
     */
    public void provideCustomPayloadToServer(CustomPayloadMethod.SenderType senderType, String senderName, String channel, Object message) {
        CustomPayloadMethod.CustomPayloadMessage msg = new CustomPayloadMethod.CustomPayloadMessage();
        msg.setSenderType(senderType);
        msg.setSenderName(senderName);
        msg.setChannel(channel);
        msg.setReceiverType(CustomPayloadMethod.ReceiverType.SERVER);
        msg.setReceiverName(serverName);
        Platform.getInstance().fillWithPayload(msg, message);
        provideCustomPayloadToServer(msg);
    }

    public void provideCustomPayloadToServer(CustomPayloadMethod.CustomPayloadMessage message) {
        if (customPayloadMessageStreamObserver != null) {
            customPayloadMessageStreamObserver.onNext(message);
        } // TODO: put it to some queue
    }
}
