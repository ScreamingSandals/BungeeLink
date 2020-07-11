package org.screamingsandals.bungeelink.bukkit;

import kr.entree.spigradle.annotations.PluginMain;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.bungeelink.ServerPlatform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.bukkit.hooks.HookManager;
import org.screamingsandals.bungeelink.bukkit.listeners.PlayerListener;
import org.screamingsandals.lib.config.ConfigAdapter;
import org.screamingsandals.lib.config.SpigotConfigAdapter;
import org.screamingsandals.lib.signs.SignListener;
import org.screamingsandals.lib.signs.SignManager;

import java.io.File;
import java.io.IOException;

@PluginMain
@Getter
public class BungeeLinkBukkitPlugin extends JavaPlugin {
    @Getter
    private static BungeeLinkBukkitPlugin instance;
    private ServerPlatform platform;
    private SignManager signManager;

    @SneakyThrows
    @Override
    public void onEnable() {
        instance = this;

        platform = new ServerPlatform();
        platform.setLogger(getLogger());

        File file = ConfigAdapter.createFile(getDataFolder(), "config.yml");
        platform.setConfigAdapter(new SpigotConfigAdapter(file) {
        });

        if (!platform.init()) {
            getLogger().severe("Initialization failed! Disabling BungeeLink... Please look at previous message from BungeeLink to solve the problem!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        platform.sayHello((ok, server) -> {
            if (!ok.equals("OK")) {
                getLogger().severe("Init connection failed! Disabling BungeeLink");
                getLogger().severe(ok);
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            platform.getCustomPayloadClientSession().init();

            server.setOnlinePlayersCount(Bukkit.getOnlinePlayers().size());
            server.setMaximumPlayersCount(Bukkit.getMaxPlayers());
            server.setMotd(Bukkit.getMotd());
            server.setServerStatus(ServerStatus.OPEN);

            getServer().getPluginManager().registerEvents(new PlayerListener(), this);
            getLogger().info("BungeeLink has been initialized");

            platform.selfUpdate();

            if (platform.getConfigAdapter().getBoolean("sign.enabled")) {

                try {
                    var signOwner = new BungeeLinkSignOwner();

                    signManager = new SignManager(signOwner, ConfigAdapter.createFile(getDataFolder(), "signs.yml"));
                    signManager.loadConfig();

                    getServer().getPluginManager().registerEvents(new SignListener(signOwner, signManager), this);

                    platform.getUpdateServerStatusDispatcher().register(server1 -> signManager.getSignsForName(server1.getServerName()).forEach(signOwner::updateSign));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            Bukkit.getScheduler().runTask(this, () -> HookManager.hook(server));
        });

    }

    @SneakyThrows
    @Override
    public void onDisable() {
        if (signManager != null) {
            signManager.save();
        }
        platform.getSyncer().shutdown();
        platform.getCustomPayloadClientSession().shutdown();
        if (platform.getClient() != null && platform.getClient().isRunning()) {
            platform.sayBye();
            platform.getClient().stop();
        }
    }
}
