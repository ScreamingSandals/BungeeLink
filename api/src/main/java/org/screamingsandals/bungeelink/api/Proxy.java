package org.screamingsandals.bungeelink.api;

import org.screamingsandals.bungeelink.api.custom.Contactable;

public interface Proxy extends Contactable {
    @Override
    default String getName() {
        return "PROXY";
    }
}
