package COMP3011.assignment1.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class ServerLifecycleService {

    private final Instant serverStart;

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
}