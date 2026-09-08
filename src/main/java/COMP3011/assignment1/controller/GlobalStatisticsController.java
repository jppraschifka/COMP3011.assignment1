package COMP3011.assignment1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import COMP3011.assignment1.dto.GlobalStatsResponse;
import COMP3011.assignment1.service.StatisticsService;

@RestController
@RequestMapping("/api/v1/global")
public class GlobalStatisticsController {

    private final StatisticsService statisticsService;

    public GlobalStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/stats")
    public GlobalStatsResponse getGlobalStats() {
        return new GlobalStatsResponse(
                statisticsService.getInputTokens(),
                statisticsService.getOutputTokens());
    }
}