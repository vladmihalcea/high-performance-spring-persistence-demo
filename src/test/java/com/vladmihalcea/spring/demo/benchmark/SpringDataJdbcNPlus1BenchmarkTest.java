package com.vladmihalcea.spring.demo.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for the Spring Data JDBC default (N+1) loading strategy.
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class SpringDataJdbcNPlus1BenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "SpringJdbcNPlus1ForumService";
    }
}

