package com.vladmihalcea.spring.demo.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for the jOOQ MULTISET strategy.
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class JooqBenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "JooqForumService";
    }
}

