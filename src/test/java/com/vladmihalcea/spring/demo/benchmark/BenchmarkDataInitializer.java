package com.vladmihalcea.spring.demo.benchmark;

import com.vladmihalcea.spring.demo.domain.jpa.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with the hierarchical data used by all multilevel benchmarks.
 * Registered as a Spring @Component so it is available in the application context
 * that each JMH benchmark fork starts via SpringApplication.run().
 *
 * @author Vlad Mihalcea
 */
@Component
public class BenchmarkDataInitializer {

    public static final int POST_COUNT = 50;
    public static final int POST_COMMENT_COUNT = 20;
    public static final int TAG_COUNT = 20;
    public static final int VOTE_COUNT = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkDataInitializer.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DataSource dataSource;

    /**
     * Idempotent: seeds the database only if the expected number of rows is not
     * already present. Called once per JMH trial (i.e. once per benchmark run).
     */
    public synchronized void ensureDataInitialized() {
        boolean initialized = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            long postCount = entityManager
                .createQuery("select count(p) from Post p", Long.class)
                .getSingleResult();
            long tagCount = entityManager
                .createQuery("select count(t) from Tag t", Long.class)
                .getSingleResult();
            long commentCount = entityManager
                .createQuery("select count(pc) from PostComment pc", Long.class)
                .getSingleResult();
            long voteCount = entityManager
                .createQuery("select count(uv) from UserVote uv", Long.class)
                .getSingleResult();
            return postCount == POST_COUNT
                && tagCount == TAG_COUNT
                && commentCount == (long) POST_COUNT * POST_COMMENT_COUNT
                && voteCount == (long) POST_COUNT * POST_COMMENT_COUNT * VOTE_COUNT;
        }));

        if (initialized) {
            LOGGER.info("Benchmark data already present – skipping seed");
            return;
        }

        LOGGER.info("Seeding benchmark data…");
        truncateData();
        seedData();
        vacuumAnalyze();
        LOGGER.info("Benchmark data seeding complete");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void truncateData() {
        executeStatement("TRUNCATE user_votes CASCADE");
        executeStatement("TRUNCATE post_comments CASCADE");
        executeStatement("TRUNCATE post_tags CASCADE");
        executeStatement("TRUNCATE tags CASCADE");
        executeStatement("TRUNCATE users CASCADE");
        executeStatement("TRUNCATE posts CASCADE");
    }

    private void seedData() {
        transactionTemplate.execute(status -> {
            User alice = new User()
                .setId("ABC123")
                .setFirstName("Alice")
                .setLastName("Smith");

            User bob = new User()
                .setId("DEF456")
                .setFirstName("Bob")
                .setLastName("Johnson");

            entityManager.persist(alice);
            entityManager.persist(bob);

            List<Tag> tags = new ArrayList<>();
            for (long i = 1; i <= TAG_COUNT; i++) {
                Tag tag = new Tag()
                    .setId(i)
                    .setName(String.format("Tag nr. %d", i));
                entityManager.persist(tag);
                tags.add(tag);
            }

            for (long postId = 1; postId <= POST_COUNT; postId++) {
                Post post = new Post()
                    .setId(postId)
                    .setTitle(String.format("Post nr. %d", postId));

                for (long i = 0; i < POST_COMMENT_COUNT; i++) {
                    PostComment comment = new PostComment()
                        .setReview("Excellent!");

                    for (int j = 0; j < VOTE_COUNT; j++) {
                        comment.addVote(
                            new UserVote()
                                .setScore(Math.random() > 0.5 ? 1 : -1)
                                .setUser(Math.random() > 0.5 ? alice : bob)
                        );
                    }

                    post.addComment(comment);
                }

                for (int i = 0; i < TAG_COUNT; i++) {
                    post.getTags().add(tags.get(i));
                }

                entityManager.persist(post);
            }

            return null;
        });
    }

    private void vacuumAnalyze() {
        executeStatement("VACUUM ANALYZE");
    }

    private void executeStatement(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            boolean autoCommit = connection.getAutoCommit();
            try {
                if (!autoCommit) {
                    connection.setAutoCommit(true);
                }
                statement.executeLargeUpdate(sql);
            } finally {
                if (!autoCommit) {
                    connection.setAutoCommit(false);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Statement failed: {}", sql, e);
        }
    }
}

