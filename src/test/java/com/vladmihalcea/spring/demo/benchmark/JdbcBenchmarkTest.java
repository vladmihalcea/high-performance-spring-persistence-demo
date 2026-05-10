package com.vladmihalcea.spring.demo.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for the two-query JDBC strategy.
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class JdbcBenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "JdbcTemplateMultipleQueryForumService";
    }
}

