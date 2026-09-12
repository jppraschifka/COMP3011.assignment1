package COMP3011.assignment1.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import COMP3011.assignment1.service.StatisticsService;

class GlobalStatisticsControllerTest {

    private MockMvc mockMvc;
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {

        statisticsService = mock(StatisticsService.class);

        GlobalStatisticsController controller =
                new GlobalStatisticsController(statisticsService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void statsReturnsCurrentTokenCounts() throws Exception {

        when(statisticsService.getInputTokens())
                .thenReturn(150L);

        when(statisticsService.getOutputTokens())
                .thenReturn(40L);

        mockMvc.perform(get("/api/v1/global/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inputTokens").value(150))
                .andExpect(jsonPath("$.outputTokens").value(40));
    }
}