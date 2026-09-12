package COMP3011.assignment1.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ConcurrentRequestTest.TestConfig.class)
class ConcurrentRequestTest {

    private static final int REQUEST_COUNT = 225;

    @LocalServerPort
    private int port;

    @Autowired
    private BlockingTestController blockingTestController;

    @Test
    void handlesMoreThan200SimultaneousBlockingRequests()
            throws Exception {

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        CountDownLatch ready =
                new CountDownLatch(REQUEST_COUNT);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<Integer>> futures =
                new ArrayList<>();

        try (ExecutorService executor =
                Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < REQUEST_COUNT; i++) {

                futures.add(executor.submit(() -> {

                    ready.countDown();
                    start.await();

                    HttpRequest request =
                            HttpRequest.newBuilder()
                                    .uri(URI.create(
                                            "http://localhost:"
                                            + port
                                            + "/test/blocking"))
                                    .GET()
                                    .build();

                    HttpResponse<String> response =
                            client.send(
                                    request,
                                    HttpResponse.BodyHandlers.ofString());

                    return response.statusCode();
                }));
            }

            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "All request tasks should be ready");

            long startTime = System.nanoTime();

            start.countDown();

            for (Future<Integer> future : futures) {
                assertEquals(
                        200,
                        future.get(10, TimeUnit.SECONDS));
            }

            double elapsedSeconds =
                    (System.nanoTime() - startTime)
                            / 1_000_000_000.0;

            assertTrue(
                    blockingTestController.getMaxActiveRequests() > 200,
                    "More than 200 requests should be "
                    + "inside the blocking endpoint simultaneously");

            assertTrue(
                    elapsedSeconds < 5.0,
                    "Concurrent requests should complete "
                    + "without significant delay");
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        BlockingTestController blockingTestController() {
            return new BlockingTestController();
        }
    }

    @RestController
    static class BlockingTestController {

        private final AtomicInteger activeRequests =
                new AtomicInteger();

        private final AtomicInteger maxActiveRequests =
                new AtomicInteger();

        @GetMapping("/test/blocking")
        public String block() throws InterruptedException {

            int current =
                    activeRequests.incrementAndGet();

            maxActiveRequests.accumulateAndGet(
                    current,
                    Math::max);

            try {

                Thread.sleep(500);

                return "OK";

            } finally {

                activeRequests.decrementAndGet();
            }
        }

        int getMaxActiveRequests() {
            return maxActiveRequests.get();
        }
    }
}