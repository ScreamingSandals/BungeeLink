package org.screamingsandals.bungeelink.api.servers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public interface ServerThirdPartyInformationHolder extends Iterable<Map.Entry<String, String>> {
    @Nullable
    String get(@NotNull String key);

    void set(@NotNull String key, @NotNull String value);

    void unset(@NotNull String key);

    boolean isSet(@NotNull String key);

    Map<String, String> toMap();

    void forEach(BiConsumer<String, String> consumer);
}
