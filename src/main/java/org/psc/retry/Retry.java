package org.psc.retry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

public class Retry {

    public static <I, O> Function<I, O> retry(Function<I, O> action) {
        return retry(action, UnaryOperator.identity());
    }

    public static <I, O> Function<I, O> retry(Function<I, O> action, UnaryOperator<RetryOptions> configure) {
        var retryOptions = new RetryOptions();
        return retryWithFunction(action, configure.apply(retryOptions), 1);
    }

    public static <T> Supplier<T> retry(Supplier<T> action) {
        return retry(action, UnaryOperator.identity());
    }

    public static <T> Supplier<T> retry(Supplier<T> action, UnaryOperator<RetryOptions> configure) {
        var retryOptions = new RetryOptions();
        return retryWithSupplier(action, configure.apply(retryOptions), 1);
    }

    private static <I, O> Function<I, O> retryWithFunction(
            Function<I, O> action,
            RetryOptions options,
            int currentRetry) {
        return input -> {
            try {
                return action.apply(input);
            } catch (Exception e) {
                return handleException(
                        e,
                        // TODO: I don't "like" the wrapping to a Supplier here, try to find a better solution
                        () -> retryWithFunction(action, options, currentRetry + 1).apply(input),
                        options,
                        currentRetry);
            }
        };
    }

    private static <T> Supplier<T> retryWithSupplier(Supplier<T> action, RetryOptions options, int currentTry) {
        return () -> {
            try {
                return action.get();
            } catch (Exception e) {
                return handleException(e, retryWithSupplier(action, options, currentTry + 1), options, currentTry);
            }
        };
    }

    private static <T> T handleException(
            Exception e,
            Supplier<T> nextInvocation,
            RetryOptions options,
            int currentRetry) {
        if (currentRetry == options.maxRetries) {
            throw new MaxRetriesExceededException(e);
        }
        return nextInvocation.get();
    }

    public static final class RetryOptions {
        private int maxRetries = 3;

        private final List<Class<? extends Exception>> exceptions = new ArrayList<>();

        private Predicate<Exception> condition;
        private Consumer<Exception> inspect;

        public RetryOptions maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        @SafeVarargs
        public final RetryOptions exceptions(Class<? extends Exception>... exceptions) {
            this.exceptions.addAll(Arrays.asList(exceptions));
            return this;
        }

        public RetryOptions condition(Predicate<Exception> condition) {
            this.condition = condition;
            return this;
        }

        public RetryOptions inspect(Consumer<Exception> inspect) {
            this.inspect = inspect;
            return this;
        }

    }

    public static class MaxRetriesExceededException extends RuntimeException {
        private MaxRetriesExceededException(Exception e) {
            super(e);
        }
    }
}
