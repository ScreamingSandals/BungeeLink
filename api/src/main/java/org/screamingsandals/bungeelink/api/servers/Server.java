package org.screamingsandals.bungeelink.api.servers;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bungeelink.api.custom.Contactable;

public interface Server extends Contactable {
    @NotNull
    String getServerName();

    @NotNull
    ServerStatus getServerStatus();

    @NotNull
    String getMotd();

    void setServerStatus(@NotNull ServerStatus serverStatus);

    int getOnlinePlayersCount();

    int getMaximumPlayersCount();

    @NotNull
    ServerThirdPartyInformationHolder getThirdPartyInformationHolder();

    @Override
    default String getName() {
        return getServerName();
    }
}
