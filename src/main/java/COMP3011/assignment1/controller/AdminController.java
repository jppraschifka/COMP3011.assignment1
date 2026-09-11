package COMP3011.assignment1.controller;

import java.time.Instant;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import COMP3011.assignment1.dto.ErrorResponse;
import COMP3011.assignment1.dto.ShutdownResponse;
import COMP3011.assignment1.dto.UptimeResponse;
import COMP3011.assignment1.service.ServerLifecycleService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ServerLifecycleService serverLifecycleService;
    private final ConfigurableApplicationContext applicationContext;

    public AdminController(
            ServerLifecycleService serverLifecycleService,
            ConfigurableApplicationContext applicationContext) {

        this.serverLifecycleService = serverLifecycleService;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/uptime")
    public UptimeResponse getServerUptime() {

        Instant now = Instant.now();

        return new UptimeResponse(
                serverLifecycleService.getServerStart().toString(),
                now.toString(),
                serverLifecycleService.getUptimeSeconds(now));
    }

    @PostMapping("/shutdown")
    public ResponseEntity<?> shutdownServer() {

        boolean accepted = serverLifecycleService.requestShutdown();

        if (!accepted) {
            ErrorResponse error = new ErrorResponse(
                    Instant.now().toString(),
                    409,
                    "Conflict",
                    "Graceful shutdown is already in progress.",
                    "/api/v1/admin/shutdown");

            return ResponseEntity.status(409).body(error);
        }

        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }

            applicationContext.close();
        });

        shutdownThread.setName("application-shutdown");
        shutdownThread.start();

        return ResponseEntity.accepted()
                .body(new ShutdownResponse(
                        "Graceful shutdown requested."));
    }
}