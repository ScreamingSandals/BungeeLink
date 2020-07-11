package org.screamingsandals.bungeelink.bukkit.hooks;

import org.screamingsandals.bungeelink.servers.Server;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class HookManager {
    private static final List<Class<? extends Hook>> hooks = List.of(ScreamingBedWarsZeroHook.class);

    public static void hook(Server thisServer) {
        hooks.forEach(hookClass -> {
            try {
                var hook = hookClass.getConstructor().newInstance();

                if (hook.isPossible()) {
                    hook.hook(thisServer);
                }
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}
