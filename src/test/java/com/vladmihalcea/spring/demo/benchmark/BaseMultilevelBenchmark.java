package com.vladmihalcea.spring.demo.benchmark;

import com.vladmihalcea.spring.demo.SpringPersistenceDemoApplication;
import com.vladmihalcea.spring.demo.domain.jpa.views.PostWithCommentsAndTagsView;
import com.vladmihalcea.spring.demo.service.PostHierarchyRetrievalService;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all multilevel-fetch JMH benchmarks.
 * Provides helper methods to start / stop the Spring application context and to
 * seed the database once per JMH trial.
 *
 * @author Vlad Mihalcea
 */
public abstract class BaseMultilevelBenchmark {

    protected ConfigurableApplicationContext context;

    protected PostHierarchyRetrievalService forumService;

    @Setup(Level.Trial)
    public void setup() {
        startSpringContext();
        forumService = context.getBean(forumServiceName(), PostHierarchyRetrievalService.class);
        ensureDataInitialized();
    }

    @Benchmark
    public List<PostWithCommentsAndTagsView> findWithCommentsAndTagsByIds() {
        return forumService.findWithCommentsAndTagsByIds(1L, 50L);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        stopSpringContext();
    }

    @Test
    public void benchmark() throws RunnerException {
        Options options = new OptionsBuilder()
            .include(getClass().getSimpleName())
            .warmupIterations(25)
            .warmupTime(TimeValue.seconds(1))
            .measurementIterations(50)
            .measurementTime(TimeValue.seconds(1))
            .mode(Mode.AverageTime)
            .timeUnit(TimeUnit.MILLISECONDS)
            .forks(1)
            .shouldFailOnError(true)
            .jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xms512m", "-Xmx1g")
            .build();
        new Runner(options).run();
    }

    /**
     * The Spring ForumService bean name to benchmark. Must be implemented by subclasses to specify the correct ForumService implementation (e.g. "JdbcTemplateCartesianProductForumService").
     *
     * @return Spring ForumService bean name
     */
    protected abstract String forumServiceName();

    /**
     * Starts the Spring Boot application context.
     */
    private void startSpringContext() {
        SpringApplicationBuilder builder =
            new SpringApplicationBuilder(SpringPersistenceDemoApplication.class)
                .web(WebApplicationType.NONE);

        Class<?>[] additionalConfigurations = additionalConfigurations();

        if (additionalConfigurations != null && additionalConfigurations.length > 0) {
            builder.sources(additionalConfigurations);
        }

        context = builder.run();
    }

    Class<?>[] additionalConfigurations() {
        return null;
    }

    /**
     * Delegates to {@link BenchmarkDataInitializer#ensureDataInitialized()}.
     * Must be called after {@link #startSpringContext}.
     */
    private void ensureDataInitialized() {
        context.getBean(BenchmarkDataInitializer.class).ensureDataInitialized();
    }

    /** Closes the Spring application context. */
    private void stopSpringContext() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

}

