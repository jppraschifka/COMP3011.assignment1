package COMP3011.assignment1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

class StatisticsServiceConcurrencyTest {

    @Test
    void concurrentTokenUpdatesDoNotLoseData()
            throws Exception {

        StatisticsService statisticsService =
                new StatisticsService();

        int threadCount = 250;
        int updatesPerThread = 100;

        long inputPerUpdate = 3;
        long outputPerUpdate = 2;

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        try (ExecutorService executor =
                Executors.newVirtualThreadPerTaskExecutor()) {

            for (int i = 0; i < threadCount; i++) {

                futures.add(executor.submit(() -> {

                    start.await();

                    for (int update = 0;
                            update < updatesPerThread;
                            update++) {

                        statisticsService.addTokenUsage(
                                inputPerUpdate,
                                outputPerUpdate);
                    }

                    return null;
                }));
            }

            start.countDown();

            for (Future<?> future : futures) {
                future.get();
            }
        }

        long expectedInput =
                threadCount
                * updatesPerThread
                * inputPerUpdate;

        long expectedOutput =
                threadCount
                * updatesPerThread
                * outputPerUpdate;

        assertEquals(
                expectedInput,
                statisticsService.getInputTokens());

        assertEquals(
                expectedOutput,
                statisticsService.getOutputTokens());
    }
}