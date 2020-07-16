package org.screamingsandals.bungeelink.fabric;

import lombok.Getter;
import lombok.SneakyThrows;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.screamingsandals.bungeelink.ServerPlatform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.fabric.util.LoggerWrapper;
import org.screamingsandals.lib.config.ConfigAdapter;
import org.screamingsandals.lib.config.GsonConfigAdapter;

import java.io.File;
import java.util.logging.Logger;

@Getter
public class BungeeLinkFabricMod implements ModInitializer {
    @Getter
    private static BungeeLinkFabricMod instance;
    private final Logger logger = new LoggerWrapper(LogManager.getLogger());
    private ServerPlatform platform;
    private File dataFolder;
    private boolean disabled = false;
    private boolean enabled = false;

    @SneakyThrows
    @Override
    public void onInitialize() {
        instance = this;

        platform = new ServerPlatform();
        platform.setLogger(logger);

        // Setup working directory
        dataFolder = new File(FabricLoader.getInstance().getConfigDirectory(), "bungeelink");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File file = ConfigAdapter.createFile(getDataFolder(), "config.json");
        platform.setConfigAdapter(GsonConfigAdapter.create(file));

        if (!platform.init()) {
            getLogger().severe("Initialization failed! Disabling BungeeLink... Please look at previous message from BungeeLink to solve the problem!");
            disabled = true;
        }
    }

    public void onServerLoaded() {
        if (disabled || enabled) {
            return;
        }

        platform.sayHello((ok, server) -> {
            if (!ok.equals("OK")) {
                getLogger().severe("Init connection failed! Disabling BungeeLink");
                getLogger().severe(ok);
                disabled = true;
                return;
            }
            enabled = true;

            platform.getCustomPayloadClientSession().init();

           /* server.setOnlinePlayersCount(Bukkit.getOnlinePlayers().size());
            server.setMaximumPlayersCount(Bukkit.getMaxPlayers());
            server.setMotd(Bukkit.getMotd());*/
            server.setServerStatus(ServerStatus.OPEN);

//            getServer().getPluginManager().registerEvents(new PlayerListener(), this);
            getLogger().info("BungeeLink has been initialized");

            platform.selfUpdate();
            /*
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
            }*/
        });
    }

    @SneakyThrows
    public void onServerShuttingDown() {
        if (enabled) {
            enabled = false;
            /*if (signManager != null) {
                signManager.save();
            }*/
            platform.getSyncer().shutdown();
            platform.getCustomPayloadClientSession().shutdown();
            if (platform.getClient() != null && platform.getClient().isRunning()) {
                platform.sayBye();
                platform.getClient().stop();
            }
        }
    }
}
