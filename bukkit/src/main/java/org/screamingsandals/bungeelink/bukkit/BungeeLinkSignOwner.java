package org.screamingsandals.bungeelink.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.servers.Server;
import org.screamingsandals.lib.signs.SignBlock;
import org.screamingsandals.lib.signs.SignOwner;

import java.util.List;
import java.util.Objects;

public class BungeeLinkSignOwner implements SignOwner {

    @Override
    public boolean isNameExists(String name) {
        return Platform.getInstance().getServerManager().getServerNames().contains(name);
    }

    @Override
    public void updateSign(SignBlock sign) {
        Bukkit.getScheduler().runTask(BungeeLinkBukkitPlugin.getInstance(), () -> {
            Server server = Platform.getInstance().getServerManager().getServer(sign.getName());
            if (server != null && sign.getLocation().getBlock().getState() instanceof Sign) {
                Sign signBlock = (Sign) sign.getLocation().getBlock().getState();
                var i = 0;
                for (var line : Platform.getInstance().getConfigAdapter().getStringList("sign.contents")) {
                    var nline = line.replace("%name%", server.getServerName())
                            .replace("%online%", String.valueOf(server.getOnlinePlayersCount()))
                            .replace("%max%", String.valueOf(server.getMaximumPlayersCount()))
                            .replace("%statusline%", server.getStatusLine())
                            .replace("%status%", server.getServerStatus().name());
                    signBlock.setLine(i, nline);
                    i++;
                }
                signBlock.update();
                if (!BungeeLinkBukkitPlugin.getInstance().getPlatform().getSyncer().areServerChangesHandled(server)) {
                    BungeeLinkBukkitPlugin.getInstance().getPlatform().getSyncer().register(server);
                }
            }
        });
    }

    @Override
    public List<String> getSignPrefixes() {
        return List.of("[bungeelink]");
    }

    @Override
    public void onClick(Player player, SignBlock sign) {
        BungeeLinkBukkitPlugin.getInstance().getPlatform().changePlayerServer(player.getUniqueId(), Objects.requireNonNull(BungeeLinkBukkitPlugin.getInstance().getPlatform().getServerByName(sign.getName())));
    }

    @Override
    public String getSignCreationPermission() {
        return "bungeelink.admin.sign";
    }

    @Override
    public String returnTranslate(String key) {
        switch (key) {
            case "sign_can_not_been_destroyed":
                return "This sign can not been destroyed!";
            case "sign_successfully_created":
                return "The sign was successfully created!";
            case "sign_can_not_been_created":
                return "The sign can not been created!";
        }
        return null;
    }

    @Override
    public void runLater(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(BungeeLinkBukkitPlugin.getInstance(), runnable, delay);
    }
}
