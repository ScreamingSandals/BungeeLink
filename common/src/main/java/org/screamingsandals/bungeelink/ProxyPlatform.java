package org.screamingsandals.bungeelink;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bungeelink.api.custom.Contactable;
import org.screamingsandals.bungeelink.features.proxy.ChangePlayerServerFeature;
import org.screamingsandals.bungeelink.features.proxy.GetPlayerServerFeature;
import org.screamingsandals.bungeelink.features.proxy.KickPlayerFeature;
import org.screamingsandals.bungeelink.network.methods.CustomPayloadMethod;
import org.screamingsandals.bungeelink.network.server.BungeeLinkServer;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.bungeelink.utils.Encryption;
import org.screamingsandals.lib.config.DefaultConfigBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class ProxyPlatform extends Platform {
    private BungeeLinkServer server;
    @Setter
    private GetPlayerServerFeature getPlayerServerFeature;
    @Setter
    private KickPlayerFeature kickPlayerFeature;
    @Setter
    private ChangePlayerServerFeature changePlayerServerFeature;

    @Override
    public boolean init() {
        if (!super.init()) {
            return false;
        }

        try {
            server = new BungeeLinkServer(getConfigAdapter().getInt("port"), getSecretToken(), getLogger(), getServerManager());

            server.start();
        } catch (Exception e) {
            getLogger().severe("We couldn't initialize server! Check your configuration!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void loadDefaults() {
        DefaultConfigBuilder.start(getConfigAdapter())
                .put("enabled", false)
                .put("port", 8082)
                .put("secretToken", Encryption.randomString(40))
                .put("servers", Map.of("exampleHub", "tokenHere"))
                .end();
    }

    @Override
    public String getSecretToken() {
        return getConfigAdapter().getString("secretToken");
    }

    @Override
    public void sendCustomPayload(String channel, Object message, Contactable contactable) {
        if (contactable instanceof Server) {
            ((Server) contactable).provideCustomPayloadToServer(CustomPayloadMethod.SenderType.PROXY, "PROXY", channel, message);
        }
    }

    @Override
    public void broadcastCustomPayload(String channel, Object message) {
        getServerManager().forEach(server1 -> server1.provideCustomPayloadToServer(CustomPayloadMethod.SenderType.PROXY, "PROXY", channel, message));
    }

    @Override
    public void getPlayerServer(@NotNull UUID uuid, @NotNull Consumer<org.screamingsandals.bungeelink.api.servers.Server> consumer) {
        var server = getPlayerServerFeature.getPlayerServer(uuid);
        consumer.accept(server);
    }

    @Override
    public void changePlayerServer(@NotNull UUID uuid, org.screamingsandals.bungeelink.api.servers.@NotNull Server server) {
        changePlayerServerFeature.changePlayerServer(uuid, (Server) server);
    }

    @Override
    public void kickPlayerFromProxy(@NotNull UUID uuid, @Nullable String reason) {
        kickPlayerFeature.kickPlayer(uuid, reason);
    }
}
