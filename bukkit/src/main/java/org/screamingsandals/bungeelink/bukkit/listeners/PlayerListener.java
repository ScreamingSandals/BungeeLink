package org.screamingsandals.bungeelink.bukkit.listeners;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.screamingsandals.bungeelink.bukkit.BungeeLinkBukkitPlugin;

import java.util.Objects;

public class PlayerListener implements Listener {

    @SneakyThrows
    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (!BungeeLinkBukkitPlugin.getInstance().getPlatform().getConfigAdapter().getBoolean("verifyUsers")) {
            return;
        }

        var future = BungeeLinkBukkitPlugin.getInstance().getPlatform().getPlayerCredentials(event.getUniqueId());
        var information = future.get();

        if (information != null && information.getPendingServer() != null && information.getPendingServer().getServerName().equals(Objects.requireNonNull(BungeeLinkBukkitPlugin.getInstance().getPlatform().getThisServerInformation()).getServerName())) {
            BungeeLinkBukkitPlugin.getInstance().getLogger().info("Player " + event.getName() + " successfully verified!");
        } else {
            BungeeLinkBukkitPlugin.getInstance().getLogger().info("Player " + event.getName() + " is not connected with correct proxy! Kicking...");
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cYou tried to connect through another proxy server!");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Objects.requireNonNull(BungeeLinkBukkitPlugin.getInstance().getPlatform().getThisServerInformation()).setOnlinePlayersCount(Bukkit.getOnlinePlayers().size());
        BungeeLinkBukkitPlugin.getInstance().getPlatform().selfUpdate();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTask(BungeeLinkBukkitPlugin.getInstance(), () -> {
            Objects.requireNonNull(BungeeLinkBukkitPlugin.getInstance().getPlatform().getThisServerInformation()).setOnlinePlayersCount(Bukkit.getOnlinePlayers().size());
            BungeeLinkBukkitPlugin.getInstance().getPlatform().selfUpdate();
        });
    }
}
