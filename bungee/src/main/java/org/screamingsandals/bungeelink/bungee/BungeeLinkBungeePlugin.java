package org.screamingsandals.bungeelink.bungee;

import kr.entree.spigradle.annotations.PluginMain;
import lombok.SneakyThrows;
import net.md_5.bungee.api.plugin.Plugin;
import org.screamingsandals.bungeelink.ProxyPlatform;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.lib.config.BungeeConfigAdapter;
import org.screamingsandals.lib.config.ConfigAdapter;

import java.io.File;

@PluginMain
public class BungeeLinkBungeePlugin extends Plugin {
    private ProxyPlatform platform;

    @SneakyThrows
    @Override
    public void onEnable() {
        platform = new ProxyPlatform();
        platform.setLogger(this.getLogger());

        File file = ConfigAdapter.createFile(getDataFolder(), "config.yml");
        platform.setConfigAdapter(new BungeeConfigAdapter(file) {
        });

        if (!platform.init()) {
            getLogger().severe("Initialization failed! Disabling BungeeLink... Please look at previous message from BungeeLink to solve the problem!");
            this.onDisable();
            return;
        }

        platform.setGetPlayerServerCallback(uuid -> {
            var player = getProxy().getPlayer(uuid);
            if (player != null && player.getServer() != null) {
                return player.getServer().getInfo().getName();
            }
            return null;
        });

        platform.setSendPlayerToServer((uuid, server) -> {
            var player = getProxy().getPlayer(uuid);
            var s = getProxy().getServerInfo(server.getServerName());
            if (player != null && s != null) {
                player.connect(s);
            }
        });

        getProxy().getServersCopy().forEach((name, server) -> {
            Server serverInstance = new Server(name);
            platform.getServerManager().addServer(serverInstance);
            if (platform.getConfigAdapter().isSet("servers." + name)) {
                serverInstance.setPublicKey(platform.getConfigAdapter().getString("servers." + name));
            }
        });

        getLogger().info("BungeeLink is listening for requests!");
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        if (platform.getServer() != null && platform.getServer().isRunning()) {
            platform.getServer().stop();
        }
    }
}