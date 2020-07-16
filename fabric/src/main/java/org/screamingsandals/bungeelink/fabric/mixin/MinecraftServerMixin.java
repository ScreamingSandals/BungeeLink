package org.screamingsandals.bungeelink.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import org.screamingsandals.bungeelink.fabric.BungeeLinkFabricMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V", ordinal = 0), method = "method_29741")
    private void afterSetupServer(CallbackInfo info) {
        BungeeLinkFabricMod.getInstance().onServerLoaded();
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void beforeShutdownServer(CallbackInfo info) {
        BungeeLinkFabricMod.getInstance().onServerShuttingDown();
    }
}
