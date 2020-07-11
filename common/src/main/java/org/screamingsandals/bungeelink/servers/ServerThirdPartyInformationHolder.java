package org.screamingsandals.bungeelink.servers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

public class ServerThirdPartyInformationHolder implements org.screamingsandals.bungeelink.api.servers.ServerThirdPartyInformationHolder {
    private Map<String, String> map = new HashMap<>();

    @Override
    public @Nullable String get(@NotNull String key) {
        return map.get(key);
    }

    @Override
    public void set(@NotNull String key, @NotNull String value) {
        map.put(key, value);
    }

    @Override
    public void unset(@NotNull String key) {
        map.remove(key);
    }

    @Override
    public boolean isSet(@NotNull String key) {
        return map.containsKey(key);
    }

    @Override
    public Map<String, String> toMap() {
        return new HashMap<>(map);
    }

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        /* work on clone to avoid concurrent exceptions */
        toMap().forEach(consumer);
    }

    /* INTERNAL ONLY */
    public void switchMap(Map<String, String> map) {
        this.map = map;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }
}
