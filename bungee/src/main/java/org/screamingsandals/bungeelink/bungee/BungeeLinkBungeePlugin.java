package org.screamingsandals.bungeelink.bungee;

import com.google.gson.internal.$Gson$Types;
import kr.entree.spigradle.annotations.PluginMain;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.plugin.Plugin;
import org.screamingsandals.bungeelink.PlayerInformation;
import org.screamingsandals.bungeelink.ProxyPlatform;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.lib.config.BungeeConfigAdapter;
import org.screamingsandals.lib.config.ConfigAdapter;

import java.io.File;
import java.util.Collection;

import static org.screamingsandals.lib.reflection.Reflection.fastInvoke;

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

        platform.setGetPlayerCredentialsFeature(uuid -> {
            var player = getProxy().getPlayer(uuid);
            if (player != null) {
                var information = new PlayerInformation(player.getName(), uuid);
                information.setAddress(player.getPendingConnection().getSocketAddress());
                if (player.getServer() != null) {
                    information.setCurrentServer(platform.getServerByName(player.getServer().getInfo().getName()));
                }
                // don't look md_5 here
                try {
                    // a bit of reflection :O
                    var pendingConnects = (Collection<ServerInfo>) fastInvoke(player, "getPendingConnects");
                    if (!pendingConnects.isEmpty()) {
                        for (ServerInfo connection : pendingConnects) {
                            information.setPendingServer(platform.getServerByName(connection.getName()));
                            break; // this looks like absolute shit. MD_5, implement it in api please
                        }
                    }
                } catch (Throwable t) {
                    getLogger().severe("Check if you have latest version of your BungeeCord proxy!");
                    t.printStackTrace();
                }
                return information;
            }
            return null;
        });

        platform.setChangePlayerServerFeature((uuid, server) -> {
            var player = getProxy().getPlayer(uuid);
            var s = getProxy().getServerInfo(server.getServerName());
            if (player != null && s != null) {
                player.connect(s);
            }
        });

        platform.setKickPlayerFeature(((uuid, reason) -> {
            var player = getProxy().getPlayer(uuid);
            if (player != null) {
                player.disconnect(TextComponent.fromLegacyText(reason != null ? reason : "You were kicked by BungeeLink"));
            }
        }));

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
