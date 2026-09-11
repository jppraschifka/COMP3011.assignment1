package COMP3011.assignment1.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class ServerLifecycleService {

    private final Instant serverStart;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

    public ServerLifecycleService() {
        this.serverStart = Instant.now();
    }

    public Instant getServerStart() {
        return serverStart;
    }

    public double getUptimeSeconds(Instant currentTime) {
        Duration uptime = Duration.between(serverStart, currentTime);
        return uptime.toNanos() / 1_000_000_000.0;
    }

    public boolean requestShutdown() {
        return shutdownRequested.compareAndSet(false, true);
    }
}