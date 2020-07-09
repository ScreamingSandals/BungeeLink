package org.screamingsandals.bungeelink.network;

import com.google.gson.Gson;
import io.grpc.MethodDescriptor;
import lombok.SneakyThrows;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.utils.Encryption;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MarshallerUtil {
    private static final Gson gson = new Gson();

    public static <T> MethodDescriptor.Marshaller<T> marshallerFor(Class<T> clz) {
        return new MethodDescriptor.Marshaller<T>() {
            @Override
            public InputStream stream(T value) {
                return new ByteArrayInputStream(Encryption.encrypt(gson.toJson(value, clz), Platform.getInstance().getSecretToken()));
            }

            @SneakyThrows
            @Override
            public T parse(InputStream stream) {
                return gson.fromJson(Encryption.decrypt(stream.readAllBytes(), Platform.getInstance().getSecretToken()), clz);
            }
        };
    }
}
