package org.screamingsandals.bungeelink.fabric.mixin;

import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLoginNetworkHandler.class)
public class LoginListenerMixin {
    // TODO: somehow add mixin to ServerLoginNetworkHandler when hasJoinedServer is called
}
