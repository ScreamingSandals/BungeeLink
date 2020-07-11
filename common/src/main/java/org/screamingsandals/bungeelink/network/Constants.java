package org.screamingsandals.bungeelink.network;

import io.grpc.Context;
import io.grpc.Metadata;

public class Constants {
    public static final byte PROTOCOL_VERSION = 2;

    public static final String METADATA_PROTOCOL_KEY = "protocol" + Metadata.BINARY_HEADER_SUFFIX;
    public static final String METADATA_IDENTITY_KEY = "identity" + Metadata.BINARY_HEADER_SUFFIX;

    public static final Context.Key<String> CONTEXT_PUBLIC_TOKEN = Context.key("public_token");
}
