package com.example.springwebflux.performance;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This class demonstrates some performance considerations with WebFlux.
 * It's not used in the application but serves as an example.
 */
public class PerformanceExample {

    // Example showing how to handle CPU-intensive operations
    public static Flux<Integer> processCpuIntensiveTask(Flux<Integer> input) {
        return input
                .publishOn(Schedulers.boundedElastic()) // Use separate thread pool for CPU-intensive work
                .map(PerformanceExample::cpuIntensiveOperation)
                .publishOn(Schedulers.parallel()); // Switch back to parallel scheduler
    }

    private static Integer cpuIntensiveOperation(Integer input) {
        // Simulate CPU-intensive operation
        int result = input;
        for (int i = 0; i < 1000000; i++) {
            result = (result * 31 + i) % 1000000007;
        }
        return result;
    }

    // Example showing how to handle backpressure
    public static Flux<Integer> handleBackpressure(Flux<Integer> input) {
        return input
                .onBackpressureBuffer(1000) // Buffer up to 1000 elements
                .limitRate(100) // Request 100 elements at a time
                .sample(Duration.ofMillis(100)); // Take one item per 100ms
    }

    // Example showing how to tune connection pool settings
    public static WebClient createOptimizedWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer size
                .build();
    }

    // Example showing parallel processing
    public static void parallelProcessing() throws InterruptedException {
        int numberOfItems = 100;
        CountDownLatch latch = new CountDownLatch(numberOfItems);

        Flux.range(1, numberOfItems)
                .flatMap(i -> Mono.fromCallable(() -> processItem(i))
                                .subscribeOn(Schedulers.boundedElastic()) // Process each item on its own thread
                                .doFinally(signal -> latch.countDown()),
                        10) // Limit concurrency to 10
                .subscribe();

        // Wait for all processing to complete
        latch.await(1, TimeUnit.MINUTES);
    }

    private static String processItem(int i) {
        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Processed " + i;
    }

    // Example of chunking large data sets
    public static Flux<Integer[]> processInChunks(Flux<Integer> input, int chunkSize) {
        return input
                .buffer(chunkSize)
                .map(list -> list.toArray(new Integer[0]));
    }

    public static void main(String[] args) throws InterruptedException {
        // Demo of CPU-intensive processing
        Flux<Integer> input = Flux.range(1, 10);
        processCpuIntensiveTask(input)
                .doOnNext(result -> System.out.println("Result: " + result))
                .blockLast(); // Block for demo purposes only

        // Demo of parallel processing
        System.out.println("Starting parallel processing...");
        parallelProcessing();
        System.out.println("Parallel processing complete.");
    }
}
