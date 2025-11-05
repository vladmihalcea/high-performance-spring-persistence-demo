package com.vladmihalcea.spring.demo.multilevel;

import com.vladmihalcea.spring.demo.BaseDemoTest;
import com.vladmihalcea.spring.demo.config.SpringPersistenceDemoConfiguration;
import com.vladmihalcea.spring.demo.domain.jpa.*;
import io.hypersistence.utils.test.transaction.VoidCallable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.Before;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@Import(SpringPersistenceDemoConfiguration.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisabledInNativeImage
@DisabledInAotMode
public abstract class BaseMultilevelDemoTest extends BaseDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public static final int POST_COUNT = 50;
	public static final int POST_COMMENT_COUNT = 20;
	public static final int TAG_COUNT = 20;
	public static final int VOTE_COUNT = 5;

	private boolean initialized;

	@Before
	public void init() {
		initialized = (
			POST_COUNT == countEntities(Post.class) &&
			TAG_COUNT == countEntities(Tag.class) &&
			POST_COUNT * POST_COMMENT_COUNT == countEntities(PostComment.class) &&
			POST_COUNT * POST_COMMENT_COUNT * VOTE_COUNT == countEntities(UserVote.class)
		);

		if(!initialized) {
			entityManagerFactory.unwrap(SessionFactoryImplementor.class)
				.getSchemaManager()
				.truncateMappedObjects();
		}
	}

	private int countEntities(Class entityClass) {
		CriteriaBuilder qb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		cq.select(qb.count(cq.from(entityClass)));
		return Math.toIntExact(entityManager.createQuery(cq).getSingleResult());
	}

	protected void addHierarchicalData() {
		if(initialized) {
			return;
		}
		executeTransaction(() -> {
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
		});
		executeStatement("VACUUM ANALYZE");
		initialized = true;
	}

	protected void executeTransaction(VoidCallable callable) {
		try {
			transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
				callable.execute();
				return null;
			});
		} catch (TransactionException e) {
			LOGGER.error("Failure", e);
		}
	}

	protected void executeStatement(String sql) {
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
			LOGGER.error("Statement failed", e);
		}
	}
}
