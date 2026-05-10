package com.vladmihalcea.spring.demo.benchmark;

import com.vladmihalcea.spring.demo.config.SpringDataJdbcSingleQueryLoadingConfiguration;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * JMH benchmark for the Spring Data JDBC single-query-loading strategy.
 *
 * @author Vlad Mihalcea
 */
@State(Scope.Benchmark)
public class SpringDataJdbcSingleQueryLoadingBenchmarkTest extends BaseMultilevelBenchmark {

    @Override
    protected String forumServiceName() {
        return "SpringJdbcSingleQueryLoadingForumService";
    }

    @Override
    Class<?>[] additionalConfigurations() {
        return new Class[]{SpringDataJdbcSingleQueryLoadingConfiguration.class};
    }
}

