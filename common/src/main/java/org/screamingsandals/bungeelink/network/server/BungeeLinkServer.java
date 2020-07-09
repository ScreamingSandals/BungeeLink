package org.screamingsandals.bungeelink.network.server;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.bungeelink.servers.ServerManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
@RequiredArgsConstructor
public class BungeeLinkServer {
    private final int port;
    private final String secretToken;
    private final Logger logger;
    private final ServerManager manager;
    private Server server;

    public void start() throws IOException {
        if (server != null) {
            throw new IllegalStateException("Server is already started!");
        }

        server = NettyServerBuilder.forPort(port).intercept(new BungeeLinkServerInterceptor(secretToken, manager)).addService(new BungeeLinkService()).build();
        server.start();
        logger.info("Server has been started on port " + port);
    }

    public void stop() throws InterruptedException {
        if (server == null) {
            throw new IllegalStateException("Server is not running!");
        }

        var s = server;
        server = null;
        s.shutdown();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        s.shutdownNow();
        if (s.awaitTermination(1, TimeUnit.SECONDS)) {
            return;
        }
        throw new RuntimeException("Unable to shutdown server");
    }

    public boolean isRunning() {
        return server != null;
    }
}
