package com.vladmihalcea.spring.demo;

import com.vladmihalcea.spring.demo.config.SpringPersistenceDemoConfiguration;
import com.vladmihalcea.spring.demo.domain.*;
import com.vladmihalcea.spring.demo.domain.views.PostCommentView;
import com.vladmihalcea.spring.demo.domain.views.PostWithCommentsAndTagsView;
import com.vladmihalcea.spring.demo.dtos.PostSummary;
import com.vladmihalcea.spring.demo.repository.HyperUserRepository;
import com.vladmihalcea.spring.demo.repository.PostCommentRepository;
import com.vladmihalcea.spring.demo.repository.PostRepository;
import com.vladmihalcea.spring.demo.repository.UserRepository;
import com.vladmihalcea.spring.demo.service.ForumService;
import io.hypersistence.utils.common.ExceptionUtil;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import io.hypersistence.utils.test.transaction.VoidCallable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.loader.MultipleBagFetchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@Import(SpringPersistenceDemoConfiguration.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisabledInNativeImage
@DisabledInAotMode
public class SpringPersistenceDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public static final int POST_COUNT = 50;
	public static final int POST_COMMENT_COUNT = 20;
	public static final int TAG_COUNT = 10;
	public static final int VOTE_COUNT = 5;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ForumService forumService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private HyperUserRepository hyperUserRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PostCommentRepository postCommentRepository;

	@Before
	public void init() {
		executeStatement("TRUNCATE user_votes CASCADE");
		executeStatement("TRUNCATE post_comments CASCADE");
		executeStatement("TRUNCATE post_tags CASCADE");
		executeStatement("TRUNCATE tags CASCADE");
		executeStatement("TRUNCATE users CASCADE");
		executeStatement("TRUNCATE posts CASCADE");
	}

	@Test
	public void testJpaRepositorySave() {
		LOGGER.info("Using JpaRepository to save a single entity");

		executeTransaction(() -> {
			userRepository.save(
				new User()
					.setId("ABC123")
					.setFirstName("Alice")
					.setLastName("Smith")
			);
		});
	}

	@Test
	public void testBaseJpaRepositoryPersist() {
		LOGGER.info("Using BaseJpaRepository to persist a single entity");

		executeTransaction(() -> {
			hyperUserRepository.persist(
				new User()
					.setId("ABC123")
					.setFirstName("Alice")
					.setLastName("Smith")
			);
		});
	}

	@Test
	public void testSaveAntiPatternSetTitle() {
		executeTransaction(() -> {
			entityManager.persist(
				new Post()
					.setId(1L)
					.setTitle("High-Performance Spring Persistence")
			);
		});

		LOGGER.info("Save Anti-Pattern - Set post title");
		Long postId = 1L;
		String title = "High-Performance Spring Persistence demo";
		forumService.setPostTitle(postId, title);

		assertEquals(title, postRepository.findById(postId).orElseThrow().getTitle());
	}

	@Test
	public void testSaveAntiPatternAddComment() {
		executeTransaction(() -> {
			Post post = new Post()
				.setId(1L)
				.setTitle("High-Performance Spring Persistence");

			for (long i = 0; i < POST_COMMENT_COUNT; i++) {
				post.addComment(
					new PostComment().setReview("Excellent!")
				);
			}

			entityManager.persist(post);
			warmUpHibernateQueryPlanCache(() ->
				postRepository.findByIdWithComments(1L)
			);
		});

		LOGGER.info("Save Anti-Pattern - Add post comment");
		Long postId = 1L;
		PostComment comment = new PostComment().setReview("High-Performance Spring Persistence rocks!");

		long addCommentStartNanos = System.nanoTime();
		forumService.addPostCommentAntiPattern(postId, comment);
		LOGGER.debug("Add comment took {} ms", elapsedMillis(addCommentStartNanos));

		assertTrue(
			postCommentRepository.findAllByPostId(postId).stream().anyMatch(
				c -> c.getReview().equals("High-Performance Spring Persistence rocks!")
			)
		);
	}

	@Test
	public void testAddCommentDirectly() {
		executeTransaction(() -> {
			Post post = new Post()
				.setId(1L)
				.setTitle("High-Performance Spring Persistence");

			for (long i = 0; i < POST_COMMENT_COUNT; i++) {
				post.addComment(
					new PostComment().setReview("Excellent!")
				);
			}

			entityManager.persist(post);
		});

		LOGGER.info("Add post comment directly");
		Long postId = 1L;
		PostComment comment = new PostComment().setReview("High-Performance Spring Persistence rocks!");

		long addCommentStartNanos = System.nanoTime();
		forumService.addPostComment(postId, comment);
		LOGGER.debug("Add comment took {} ms", elapsedMillis(addCommentStartNanos));

		assertTrue(
			postCommentRepository.findAllByPostId(postId).stream().anyMatch(
				c -> c.getReview().equals("High-Performance Spring Persistence rocks!")
			)
		);
	}

	@Test
	public void testBaseJpaRepositoryPersistAll() {
		LOGGER.info("Using BaseJpaRepository to persist multiple entities");

		executeTransaction(() -> {
			hyperUserRepository.persistAll(List.of(
				new User()
					.setId("ABC123")
					.setFirstName("Alice")
					.setLastName("Smith"),
				new User()
					.setId("DEF456")
					.setFirstName("Bob")
					.setLastName("Johnson"),
				new User()
					.setId("GHI789")
					.setFirstName("Carol")
					.setLastName("Williams")
			));
		});
	}

	@Test
	public void testBaseJpaRepositoryPersistAllWithSqlCountValidator() {
		LOGGER.info("Using BaseJpaRepository to persist multiple entities");

		SQLStatementCountValidator.reset();
		executeTransaction(() -> {
			hyperUserRepository.persistAll(List.of(
				new User()
					.setId("ABC123")
					.setFirstName("Alice")
					.setLastName("Smith"),
				new User()
					.setId("DEF456")
					.setFirstName("Bob")
					.setLastName("Johnson"),
				new User()
					.setId("GHI789")
					.setFirstName("Carol")
					.setLastName("Williams")
			));
		});
		SQLStatementCountValidator.assertInsertCount(1);
		SQLStatementCountValidator.assertSelectCount(0);
	}

	@Test
	public void testFindNPlusOne() {
		LOGGER.info("Finding N+1 query issues during testing");

		final int POST_COUNT_SIZE = 5;

		executeTransaction(() -> {
			for (long postId = 1; postId <= POST_COUNT; postId++) {
				Post post = new Post()
					.setId(postId)
					.setTitle(String.format("Post nr. %d", postId));

				entityManager.persist(post);
			}
			warmUpHibernateQueryPlanCache(() ->
				postRepository.firstLatestPostSummariesAntiPattern(1)
			);
		});

		executeTransaction(() -> {
			SQLStatementCountValidator.reset();
			long startNanos = System.nanoTime();

			List<PostSummary> postSummaries = postRepository.firstLatestPostSummariesAntiPattern(POST_COUNT_SIZE);

			LOGGER.debug("Fetching {} PostSummary objects took {} ms", POST_COUNT_SIZE, elapsedMillis(startNanos));
			SQLStatementCountValidator.assertSelectCount(1);

			assertEquals(POST_COUNT_SIZE, postSummaries.size());
		});
	}

	@Test
	public void testFixNPlusOneWithDTOProjection() {
		LOGGER.info("Fixing the N+1 query issue with DTO projection");

		final int POST_COUNT_SIZE = 5;

		executeTransaction(() -> {
			for (long postId = 1; postId <= POST_COUNT; postId++) {
				Post post = new Post()
					.setId(postId)
					.setTitle(String.format("Post nr. %d", postId));

				entityManager.persist(post);
			}
			warmUpHibernateQueryPlanCache(() ->
				postRepository.firstLatestPostSummaries(1)
			);
		});

		executeTransaction(() -> {
			SQLStatementCountValidator.reset();
			long startNanos = System.nanoTime();
			List<PostSummary> postSummaries = postRepository.firstLatestPostSummaries(POST_COUNT_SIZE);

			LOGGER.debug("Fetching {} PostSummary objects took {} ms", POST_COUNT_SIZE, elapsedMillis(startNanos));
			SQLStatementCountValidator.assertSelectCount(1);

			assertEquals(POST_COUNT_SIZE, postSummaries.size());
		});
	}

	@Test
	public void testWithCartesianProduct() {
		addHierarchicalData();

		executeTransaction(() -> {
			try {
				List<Post> posts = entityManager.createQuery("""
                    select p
                    from Post p
                    left join fetch p.tags t
                    left join fetch p.comments pc
                    left join fetch pc.votes v
                    left join fetch v.user u
                    where p.id between :minId and :maxId
                    """, Post.class)
				.setParameter("minId", 1L)
				.setParameter("maxId", 50L)
				.getResultList();

				fail("Should have thrown MultipleBagFetchException");
			} catch (IllegalArgumentException e) {
				LOGGER.info("Expected", e);
				assertEquals(MultipleBagFetchException.class, ExceptionUtil.rootCause(e).getClass());
			}
		});
	}

	@Test
	public void testWithProgressiveJoinFetching() {
		addHierarchicalData();

		warmUpHibernateQueryPlanCache(() ->
			forumService.findWithCommentsAndTagsByIds(1L, 50L)
		);

		long startNanos = System.nanoTime();
		List<Post> posts = forumService.findWithCommentsAndTagsByIds(
			1L, 50L
		);
		LOGGER.debug("Fetching 50 Posts with comments and tags took {} ms", elapsedMillis(startNanos));

		assertEquals(POST_COUNT, posts.size());

		for (Post post : posts) {
			assertEquals(POST_COMMENT_COUNT, post.getComments().size());
			for(PostComment comment : post.getComments()) {
				assertEquals(VOTE_COUNT, comment.getVotes().size());
			}
			assertEquals(TAG_COUNT, post.getTags().size());
		}
	}

	@Test
	public void testMultiLevelFetchingWithMultiset() {
		addHierarchicalData();
		warmUpHibernateQueryPlanCache(() ->
			forumService.findPostWithCommentsAndTagsViewByIds(1L, 50L)
		);

		long startNanos = System.nanoTime();
		List<PostWithCommentsAndTagsView> posts = forumService.findPostWithCommentsAndTagsViewByIds(
			1L, 50L
		);
		LOGGER.debug("Fetching 50 Posts with comments and tags took {} ms", elapsedMillis(startNanos));

		assertEquals(POST_COUNT, posts.size());

		for (PostWithCommentsAndTagsView post : posts) {
			assertEquals(POST_COMMENT_COUNT, post.getComments().size());
			for(PostCommentView comment : post.getComments()) {
				assertEquals(VOTE_COUNT, comment.getVotes().size());
			}
			assertEquals(TAG_COUNT, post.getTags().size());
		}
	}

	private void addHierarchicalData() {
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
	}

	private void executeTransaction(VoidCallable callable) {
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

	private void warmUpHibernateQueryPlanCache(VoidCallable function) {
		function.execute();
	}
}
