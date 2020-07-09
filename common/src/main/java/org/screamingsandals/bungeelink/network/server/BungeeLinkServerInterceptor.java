package org.screamingsandals.bungeelink.network.server;

import com.google.gson.Gson;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.bungeelink.servers.ServerManager;
import org.screamingsandals.bungeelink.utils.Encryption;

import java.util.Map;

@RequiredArgsConstructor
public class BungeeLinkServerInterceptor implements ServerInterceptor {
    private static final Gson gson = new Gson();

    private final String secret;
    private final ServerManager serverManager;

    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
    };

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        var protocolKey = Metadata.Key.of(Constants.METADATA_PROTOCOL_KEY, Metadata.BINARY_BYTE_MARSHALLER);
        var idKey = Metadata.Key.of(Constants.METADATA_IDENTITY_KEY, Metadata.BINARY_BYTE_MARSHALLER);

        var protocol = headers.get(protocolKey);
        if (protocol == null || protocol.length == 0) {
            call.close(Status.UNAUTHENTICATED.withDescription("Protocol version is not provided by client!"), headers);
            return NOOP_LISTENER;
        }
        if (protocol[0] != Constants.PROTOCOL_VERSION) {
            call.close(Status.UNAUTHENTICATED.withDescription("Incompatible protocol version"), headers);
            return NOOP_LISTENER;
        }

        var identity = headers.get(idKey);

        String token;

        try {
            var decrypted = gson.fromJson(Encryption.decrypt(identity, secret), Map.class);
            token = decrypted.get("public").toString();
            var time = ((Number) decrypted.get("time")).longValue();
            if (System.currentTimeMillis() - 60000 > time) {
                call.close(Status.UNAUTHENTICATED.withDescription("Identity key is too old"), headers);
                return NOOP_LISTENER;
            }
        } catch (Exception e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Identity key is invalid!"), headers);
            return NOOP_LISTENER;
        }

        if (token == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Token is not provided"), headers);
            return NOOP_LISTENER;
        }

        Server server = serverManager.getServerByPublicKey(token);
        if (server == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Provided token is invalid"), headers);
            return NOOP_LISTENER;
        }

        Context context = Context.current().withValue(Constants.CONTEXT_PUBLIC_TOKEN, token);
        return Contexts.interceptCall(context, call, headers, next);
    }
}
