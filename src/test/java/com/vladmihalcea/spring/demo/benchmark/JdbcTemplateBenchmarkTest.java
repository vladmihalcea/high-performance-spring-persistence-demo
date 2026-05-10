package com.vladmihalcea.spring.demo.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for the JdbcTemplate single-query + row-mapper strategy.
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class JdbcTemplateBenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "JdbcTemplateCartesianProductForumService";
    }
}

