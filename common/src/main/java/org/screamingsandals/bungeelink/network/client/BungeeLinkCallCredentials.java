package org.screamingsandals.bungeelink.network.client;

import com.google.gson.Gson;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bungeelink.network.Constants;
import org.screamingsandals.bungeelink.utils.Encryption;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executor;

@Getter
@RequiredArgsConstructor
public class BungeeLinkCallCredentials extends CallCredentials {
    private static final Gson gson = new Gson();

    private final String token;
    private final String secret;

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(() -> {
            try {
                var headers = new Metadata();
                var protocolKey = Metadata.Key.of(Constants.METADATA_PROTOCOL_KEY, Metadata.BINARY_BYTE_MARSHALLER);
                var identity = Map.of(
                    "public", token,
                        "time", System.currentTimeMillis()
                );
                var idKey = Metadata.Key.of(Constants.METADATA_IDENTITY_KEY, Metadata.BINARY_BYTE_MARSHALLER);
                headers.put(protocolKey, new byte[] {Constants.PROTOCOL_VERSION});
                headers.put(idKey, Encryption.encrypt(gson.toJson(identity), secret));
                applier.apply(headers);
            } catch (Throwable e) {
                applier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
        // For what is this? :D
    }
}
