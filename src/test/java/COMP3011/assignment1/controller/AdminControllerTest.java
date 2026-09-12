package COMP3011.assignment1.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.timeout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import COMP3011.assignment1.service.ServerLifecycleService;

class AdminControllerTest {

    private MockMvc mockMvc;
    private ServerLifecycleService lifecycleService;
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void setUp() {

        lifecycleService = mock(ServerLifecycleService.class);

        applicationContext =
                mock(ConfigurableApplicationContext.class);

        AdminController controller =
                new AdminController(
                        lifecycleService,
                        applicationContext);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void uptimeReturnsRequiredFields() throws Exception {

        Instant start =
                Instant.parse("2026-09-12T00:00:00Z");

        when(lifecycleService.getServerStart())
                .thenReturn(start);

        when(lifecycleService.getUptimeSeconds(
                org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(120.5);

        mockMvc.perform(get("/api/v1/admin/uptime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utcServerStart")
                        .value("2026-09-12T00:00:00Z"))
                .andExpect(jsonPath("$.utcNow").exists())
                .andExpect(jsonPath("$.serverUptimeSeconds")
                        .value(120.5));
    }

    @Test
    void shutdownReturnsAccepted() throws Exception {

        when(lifecycleService.requestShutdown())
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/shutdown"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message")
                        .value("Graceful shutdown requested."));

        verify(applicationContext, timeout(1500))
                .close();
    }

    @Test
    void repeatedShutdownReturnsConflict() throws Exception {

        when(lifecycleService.requestShutdown())
                .thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/shutdown"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                            "Graceful shutdown is already in progress."))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/admin/shutdown"));
    }
}