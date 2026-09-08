package COMP3011.assignment1.controller;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import COMP3011.assignment1.dto.UptimeResponse;
import COMP3011.assignment1.service.ServerLifecycleService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ServerLifecycleService serverLifecycleService;

    public AdminController(ServerLifecycleService serverLifecycleService) {
        this.serverLifecycleService = serverLifecycleService;
    }

    @GetMapping("/uptime")
    public UptimeResponse getServerUptime() {

        Instant now = Instant.now();

        return new UptimeResponse(
                serverLifecycleService.getServerStart().toString(),
                now.toString(),
                serverLifecycleService.getUptimeSeconds(now));
    }
}