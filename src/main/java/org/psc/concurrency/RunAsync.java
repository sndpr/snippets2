package org.psc.concurrency;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class RunAsync {

    public static void main(String[] args) throws InterruptedException {
        log.info("main() started");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(RunAsync::runMe);
        log.info("main() completed");
        shutdownAndAwaitTermination(executorService);
        log.info("shutdown completed");
        ForkJoinPool.commonPool().awaitTermination(10L, TimeUnit.SECONDS);
    }

    public static void runMe() {
        CompletableFuture.runAsync(() -> {
            log.info("async started");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("finally completed");
        });
        CompletableFuture.delayedExecutor(1L, TimeUnit.SECONDS)
                .execute(() -> log.info("delayed"));
        log.info("runMe() completed");
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
