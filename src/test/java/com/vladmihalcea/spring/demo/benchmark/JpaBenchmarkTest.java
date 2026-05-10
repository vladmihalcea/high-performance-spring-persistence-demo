package com.vladmihalcea.spring.demo.benchmark;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for JPA progressive-join-fetch strategy.
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class JpaBenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "JpaForumService";
    }
}

