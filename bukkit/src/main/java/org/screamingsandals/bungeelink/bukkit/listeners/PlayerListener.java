package org.screamingsandals.bungeelink.bukkit.listeners;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.screamingsandals.bungeelink.bukkit.BungeeLinkBukkitPlugin;

import java.util.Objects;

public class PlayerListener implements Listener {

    /* TODO: Think about AsyncPlayerPreLoginEvent (but we need to somehow manage this on bungee side, maybe API pull request?) */
    @SneakyThrows
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (!BungeeLinkBukkitPlugin.getInstance().getPlatform().getConfigAdapter().getBoolean("verifyUsers")) {
            return;
        }

        BungeeLinkBukkitPlugin.getInstance().getPlatform().getPlayerServer(event.getPlayer().getUniqueId(), server -> {
            if (server != null && server.getServerName().equals(Objects.requireNonNull(BungeeLinkBukkitPlugin.getInstance().getPlatform().getThisServerInformation()).getServerName())) {
                BungeeLinkBukkitPlugin.getInstance().getLogger().info("Player " + event.getPlayer().getName() + " successfully verified!");
            } else {
                BungeeLinkBukkitPlugin.getInstance().getLogger().info("Player " + event.getPlayer().getName() + " is not connected with correct proxy! Kicking...");
                Bukkit.getScheduler().runTask(BungeeLinkBukkitPlugin.getInstance(), () -> event.getPlayer().kickPlayer("§cYou tried to connect through another proxy server!"));
            }
        });
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
