package org.screamingsandals.bungeelink.network.client;

import io.grpc.*;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


@Getter
@RequiredArgsConstructor
public class BungeeLinkClient {
    private final InetAddress address;
    private final int port;
    private final String publicToken;
    private final String secretToken;
    private final Logger logger;
    private BungeeLinkCallCredentials credentials;
    private ManagedChannel channel;

    public void start() {
        if (channel != null) {
            throw new IllegalStateException("Channel is already started!");
        }

        credentials = new BungeeLinkCallCredentials(publicToken, secretToken);

        channel = NettyChannelBuilder.forAddress(address.getHostAddress(), port)
                .usePlaintext()
                .build();
    }

    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> initCall(MethodDescriptor<RequestT, ResponseT> methodDescriptor) {
        return channel.newCall(methodDescriptor, CallOptions.DEFAULT.withCallCredentials(credentials));
    }

    public <RequestT, ResponseT> StreamObserver<RequestT> initStreamCall(MethodDescriptor<RequestT, ResponseT> methodDescriptor, StreamObserver<ResponseT> observer) {
        return ClientCalls.asyncBidiStreamingCall(initCall(methodDescriptor), observer);
    }

    public void stop() throws InterruptedException {
        if (channel == null) {
            throw new IllegalStateException("Channel is not opened!");
        }

        var s = channel;
        channel = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new RuntimeException("Unable to close channel");
    }

    public boolean isRunning() {
        return channel != null;
    }
}
