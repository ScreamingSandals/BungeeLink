package org.screamingsandals.bungeelink.fabric.mixin;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.screamingsandals.bungeelink.fabric.BungeeLinkFabricMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow
    private List<ServerPlayerEntity> players;

    @SneakyThrows
    @Inject(at = @At("HEAD"), method = "checkCanJoin", cancellable = true)
    public void checkCanJoin(SocketAddress socketAddress, GameProfile profile, CallbackInfoReturnable<Text> info) {
        if (BungeeLinkFabricMod.getInstance().isEnabled() && BungeeLinkFabricMod.getInstance().getPlatform().getConfigAdapter().getBoolean("verifyUsers")) {
            var future = BungeeLinkFabricMod.getInstance().getPlatform().getPlayerCredentials(profile.getId());
            var information = future.get();

            if (information != null && information.getPendingServer() != null && information.getPendingServer().getServerName().equals(Objects.requireNonNull(BungeeLinkFabricMod.getInstance().getPlatform().getThisServerInformation()).getServerName())) {
                BungeeLinkFabricMod.getInstance().getLogger().info("Player " + profile.getName() + " successfully verified!");
            } else {
                BungeeLinkFabricMod.getInstance().getLogger().info("Player " + profile.getName() + " is not connected with correct proxy! Kicking...");
                info.setReturnValue(new LiteralText("§cYou tried to connect through another proxy server!"));
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "onPlayerConnect")
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity playerEntity, CallbackInfo info) {
        if (BungeeLinkFabricMod.getInstance().isEnabled()) {
            Objects.requireNonNull(BungeeLinkFabricMod.getInstance().getPlatform().getThisServerInformation()).setOnlinePlayersCount(players.size());
            BungeeLinkFabricMod.getInstance().getPlatform().selfUpdate();
        }
    }

    @Inject(at = @At("HEAD"), method = "remove")
    public void onQuit(ServerPlayerEntity playerEntity, CallbackInfo info) {
        if (BungeeLinkFabricMod.getInstance().isEnabled()) {
            Objects.requireNonNull(BungeeLinkFabricMod.getInstance().getPlatform().getThisServerInformation()).setOnlinePlayersCount(players.size() - 1);
            BungeeLinkFabricMod.getInstance().getPlatform().selfUpdate();
        }
    }
}
