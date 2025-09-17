package com.vladmihalcea.spring.util;

import java.util.concurrent.TimeUnit;

/**
 * @author Vlad Mihalcea
 */
public class Utils {

    public static long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
