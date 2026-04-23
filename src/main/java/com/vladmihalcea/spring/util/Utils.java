package com.vladmihalcea.spring.util;

import java.util.concurrent.TimeUnit;

/**
 * @author Vlad Mihalcea
 */
public class Utils {

    public static long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    public static <T> T uncheck(CheckedFunc<T> func) {
        try {
            return func.apply();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface CheckedFunc<R> {
        R apply() throws Exception;
    }
}
