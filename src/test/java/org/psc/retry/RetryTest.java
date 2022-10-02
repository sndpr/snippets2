package org.psc.retry;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryTest {

    @Test
    void testFunctionRetryForever() {
        var atomicCounter = new AtomicInteger(0);
        Function<String, String> failingToUpperCase = s -> {
            if (atomicCounter.get() < 2) {
                atomicCounter.getAndIncrement();
                throw new IllegalStateException("not yet");
            } else {
                return s.toUpperCase(Locale.ROOT);
            }
        };

        var retryForever = Retry.retry(failingToUpperCase);
        assertThat(retryForever.apply("hello")).isEqualTo("HELLO");
    }

    @Test
    void testFunctionMaxRetriesExceededException() {
        Function<String, String> failingFunction = s -> {
            throw new IllegalStateException("no");
        };

        assertThrows(Retry.MaxRetriesExceededException.class, () -> Retry.retry(failingFunction).apply("s"));
    }

    @Test
    void testSupplierRetryForever() {
        var atomicCounter = new AtomicInteger(0);
        Supplier<Integer> failingSupplier = () -> {
            if (atomicCounter.get() < 2) {
                atomicCounter.getAndIncrement();
                throw new IllegalStateException("not yet");
            } else {
                return 42;
            }
        };

        var retryForever = Retry.retry(failingSupplier);
        assertThat(retryForever.get()).isEqualTo(42);
    }

    @Test
    void testSupplierMaxRetriesExceededException() {
        Supplier<Integer> failingSupplier = () -> {
            throw new IllegalStateException("no");
        };

        assertThrows(Retry.MaxRetriesExceededException.class, () -> Retry.retry(failingSupplier).get());
    }
}