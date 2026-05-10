package com.vladmihalcea.spring.demo.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for Blaze Persistence MULTISET / Entity-View strategy.
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class BlazePersistenceBenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "JpaForumService";
    }
}

