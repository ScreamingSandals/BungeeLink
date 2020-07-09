package org.screamingsandals.bungeelink;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bungeelink.api.BungeeLinkAPI;
import org.screamingsandals.bungeelink.api.custom.Contactable;
import org.screamingsandals.bungeelink.custom.CustomPayloadManager;
import org.screamingsandals.bungeelink.event.UpdateServerStatusDispatcher;
import org.screamingsandals.bungeelink.network.methods.CustomPayloadMethod;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.bungeelink.servers.ServerManager;
import org.screamingsandals.lib.config.ConfigAdapter;

import java.util.function.Consumer;
import java.util.logging.Logger;

@Getter
public abstract class Platform extends BungeeLinkAPI {
    @Getter
    private static Platform instance;
    private final ServerManager serverManager;
    private final UpdateServerStatusDispatcher updateServerStatusDispatcher;
    private final CustomPayloadManager customPayloadManager;
    @Setter
    private Logger logger;
    @Setter
    private ConfigAdapter configAdapter;

    public Platform() {
        BungeeLinkAPI.api = Platform.instance = this;
        this.serverManager = new ServerManager();
        updateServerStatusDispatcher = new UpdateServerStatusDispatcher();
        customPayloadManager = new CustomPayloadManager();
    }

    public boolean init() {
        if (configAdapter == null) {
            throw new IllegalStateException("ConfigAdapter is not provided!");
        }

        configAdapter.load();

        loadDefaults();
        if (!getConfigAdapter().getBoolean("enabled")) {
            getLogger().severe("Plugin needs to be configured! Locate config.yml (on velocity config.toml) file in BungeeLink folder");
            return false;
        }

        return true;
    }

    public abstract void loadDefaults();

    public abstract String getSecretToken();

    public abstract void sendCustomPayload(String channel, Object message, Contactable contactable);

    public abstract void broadcastCustomPayload(String channel, Object message);


    @Override
    public @Nullable Server getThisServerInformation() {
        return null;
    }

    @Override
    public void processUpdate() {
        // nothing
    }

    @Override
    public Server getServerByName(@NotNull String serverName) {
        return serverManager.getServer(serverName);
    }

    @Override
    public void registerServerDataListener(@NotNull String serverName, @NotNull Consumer<org.screamingsandals.bungeelink.api.servers.Server> listener) {
        Server server = serverManager.getServer(serverName);
        if (server == null) {
            return;
        }

        updateServerStatusDispatcher.register(server, listener::accept);
    }

    @Override
    public @NotNull CustomPayloadManager getCustomPayloadManager() {
        return this.customPayloadManager;
    }

    public void fillWithPayload(CustomPayloadMethod.CustomPayloadMessage message, Object payload) {
        Gson gson = new Gson();
        String json = gson.toJson(payload, payload.getClass());
        message.setPayloadClassName(payload.getClass().getCanonicalName());
        message.setPayload(json);
    }

    @SneakyThrows
    public Object getPayload(CustomPayloadMethod.CustomPayloadMessage message) {
        Gson gson = new Gson();
        return gson.fromJson(message.getPayload(), Class.forName(message.getPayloadClassName()));
    }

    public Contactable resloveSender(CustomPayloadMethod.SenderType senderType, String senderName) {
        if (senderType == CustomPayloadMethod.SenderType.PROXY) {
            return getCustomPayloadManager().getProxy();
        }
        return getServerManager().getServer(senderName);
    }


}
