package org.screamingsandals.bungeelink.api.servers;

import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bungeelink.api.custom.Contactable;

public interface Server extends Contactable {
    @NotNull
    String getServerName();

    @NotNull
    ServerStatus getServerStatus();

    @NotNull
    String getStatusLine();

    void setStatusLine(@NotNull String statusLine);

    void setServerStatus(@NotNull ServerStatus serverStatus);

    int getOnlinePlayersCount();

    int getMaximumPlayersCount();

    @Override
    default String getName() {
        return getServerName();
    }
}
