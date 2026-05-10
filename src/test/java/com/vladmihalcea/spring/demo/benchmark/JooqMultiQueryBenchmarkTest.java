package com.vladmihalcea.spring.demo.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for the jOOQ multi-query strategy (2 queries: Posts+Tags, PostComments+Votes).
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class JooqMultiQueryBenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "JooqMultiQueryForumService";
    }
}

