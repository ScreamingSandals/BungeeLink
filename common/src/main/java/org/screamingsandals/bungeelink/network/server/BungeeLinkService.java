package org.screamingsandals.bungeelink.network.server;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;
import org.screamingsandals.bungeelink.network.methods.*;

public class BungeeLinkService implements BindableService {
    public static final String SERVICE_NAME = BungeeLinkService.class.getCanonicalName();

    @Override
    public ServerServiceDefinition bindService() {
        var ssd = ServerServiceDefinition.builder(SERVICE_NAME);
        ssd.addMethod(HelloServerMethod.METHOD, ServerCalls.asyncUnaryCall(HelloServerMethod::onRequest));
        ssd.addMethod(UpdateServerStatusMethod.METHOD, ServerCalls.asyncUnaryCall(UpdateServerStatusMethod::onRequest));
        ssd.addMethod(ServerStatusMethod.METHOD, ServerCalls.asyncBidiStreamingCall(ServerStatusMethod::onRequest));
        ssd.addMethod(GetPlayerServerMethod.METHOD, ServerCalls.asyncUnaryCall(GetPlayerServerMethod::onRequest));
        ssd.addMethod(SendPlayerMethod.METHOD, ServerCalls.asyncUnaryCall(SendPlayerMethod::onRequest));
        ssd.addMethod(CustomPayloadMethod.METHOD, ServerCalls.asyncBidiStreamingCall(CustomPayloadMethod::onRequest));
        ssd.addMethod(KickPlayerMethod.METHOD, ServerCalls.asyncUnaryCall(KickPlayerMethod::onRequest));
        ssd.addMethod(ByeServerMethod.METHOD, ServerCalls.asyncUnaryCall(ByeServerMethod::onRequest));
        return ssd.build();
    }
}
