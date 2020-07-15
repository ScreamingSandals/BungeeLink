package org.screamingsandals.bungeelink.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.SneakyThrows;
import net.kyori.text.TextComponent;
import org.screamingsandals.bungeelink.PlayerInformation;
import org.screamingsandals.bungeelink.ProxyPlatform;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.bungeelink.velocity.util.LoggerWrapper;
import org.screamingsandals.lib.config.ConfigAdapter;
import org.screamingsandals.lib.config.TomlConfigAdapter;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static org.screamingsandals.lib.reflection.Reflection.fastInvoke;

@Plugin(id = "bungeelink", name = "BungeeLink", version = VersionInfo.VERSION, authors = {"ScreamingSandals"})
public class BungeeLinkVelocityPlugin {

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer proxyServer;
    @Inject
    @DataDirectory
    private Path dataFolder;
    private ProxyPlatform platform;

    @SneakyThrows
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        System.out.println(Arrays.toString(getClass().getDeclaredField("logger").getAnnotations()));

        platform = new ProxyPlatform();
        platform.setLogger(new LoggerWrapper(logger));

        File file = ConfigAdapter.createFile(dataFolder.toFile(), "config.toml");
        platform.setConfigAdapter(TomlConfigAdapter.create(file));

        if (!platform.init()) {
            logger.error("Initialization failed! Disabling BungeeLink... Please look at previous message from BungeeLink to solve the problem!");
            return;
        }

        platform.setGetPlayerCredentialsFeature(uuid -> {
            var player = proxyServer.getPlayer(uuid);
            if (player.isPresent() && player.get().getCurrentServer().isPresent()) {
                var p = player.get();
                var information = new PlayerInformation(p.getUsername(), uuid);
                information.setAddress(p.getRemoteAddress());
                p.getCurrentServer().ifPresent(serverConnection -> information.setCurrentServer(platform.getServerByName(serverConnection.getServerInfo().getName())));
                // don't look velocity maintainers here
                try {
                    // a bit of reflection :O
                    var connectionInFlight = (ServerConnection) fastInvoke(player, "getConnectionInFlight");
                    if (connectionInFlight != null) {
                        information.setPendingServer(platform.getServerByName(connectionInFlight.getServerInfo().getName()));
                    }
                } catch (Throwable t) {
                    logger.error("Check if you have latest version of your Velocity proxy!");
                    t.printStackTrace();
                }
                return information;
            }
            return null;
        });

        platform.setChangePlayerServerFeature((uuid, server) -> {
            var player = proxyServer.getPlayer(uuid);
            var s = proxyServer.getServer(server.getServerName());
            if (player.isPresent() && s.isPresent()) {
                player.get().createConnectionRequest(s.get()).connect();
            }
        });

        platform.setKickPlayerFeature(((uuid, reason) -> {
            var player = proxyServer.getPlayer(uuid);
            player.ifPresent(value -> value.disconnect(TextComponent.of(reason != null ? reason : "You were kicked by BungeeLink")));
        }));

        proxyServer.getAllServers().forEach((server) -> {
            Server serverInstance = new Server(server.getServerInfo().getName());
            platform.getServerManager().addServer(serverInstance);
            if (platform.getConfigAdapter().isSet("servers." + serverInstance.getServerName())) {
                serverInstance.setPublicKey(platform.getConfigAdapter().getString("servers." + serverInstance.getServerName()));
            }
        });

        logger.info("BungeeLink is listening for requests!");
    }

    @SneakyThrows
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (platform.getServer() != null && platform.getServer().isRunning()) {
            platform.getServer().stop();
        }
    }
}
