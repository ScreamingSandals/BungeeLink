package org.screamingsandals.bungeelink.bukkit.hooks;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.events.BedwarsGameStartedEvent;
import org.screamingsandals.bedwars.api.events.BedwarsPreRebuildingEvent;
import org.screamingsandals.bungeelink.Platform;
import org.screamingsandals.bungeelink.api.servers.ServerStatus;
import org.screamingsandals.bungeelink.bukkit.BungeeLinkBukkitPlugin;
import org.screamingsandals.bungeelink.servers.Server;

public class ScreamingBedWarsZeroHook implements Hook, Listener {
    private Server thisServer;

    @Override
    public boolean isPossible() {
        try {
            Class.forName("org.screamingsandals.bedwars.api.BedwarsAPI");

            BedwarsAPI api = BedwarsAPI.getInstance();
            if (api.getPluginVersion().startsWith("0.")) { // this hook is only for bw 0.x.x (TODO: make one for 1.x.x)
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void hook(Server thisServer) {
        BedwarsAPI api = BedwarsAPI.getInstance();

        for (var game : api.getGames()) {
            if (game.getBungeeEnabled()) {
                this.thisServer = thisServer;
                Bukkit.getPluginManager().registerEvents(this, BungeeLinkBukkitPlugin.getInstance());

                var holder = thisServer.getThirdPartyInformationHolder();
                holder.set("prefix", "[BedWars]");
                holder.set("bw_game", game.getName());
                holder.set("bw_status", "Waiting");

                thisServer.setMaximumPlayersCount(game.getMaxPlayers());

                Platform.getInstance().processUpdate();
                break;
            }
        }
    }

    @EventHandler
    public void onGamePreRebuild(BedwarsPreRebuildingEvent event) {
        var holder = thisServer.getThirdPartyInformationHolder();
        holder.set("bw_status", "Rebuilding");

        Platform.getInstance().processUpdate();
    }

    @EventHandler
    public void onGameStart(BedwarsGameStartedEvent event) {
        thisServer.setServerStatus(ServerStatus.CLOSED_FOR_NEW_PLAYERS);

        var holder = thisServer.getThirdPartyInformationHolder();
        holder.set("bw_status", "Running");

        Platform.getInstance().processUpdate();
    }
}
