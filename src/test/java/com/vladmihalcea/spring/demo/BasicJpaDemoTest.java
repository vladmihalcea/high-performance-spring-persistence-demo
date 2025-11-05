package com.vladmihalcea.spring.demo;

import com.vladmihalcea.spring.demo.domain.jpa.Post;
import com.vladmihalcea.spring.demo.domain.jpa.PostComment;
import com.vladmihalcea.spring.demo.domain.jpa.User;
import com.vladmihalcea.spring.demo.domain.jpa.dtos.PostSummary;
import com.vladmihalcea.spring.demo.repository.jpa.HyperUserRepository;
import com.vladmihalcea.spring.demo.repository.jpa.PostCommentRepository;
import com.vladmihalcea.spring.demo.repository.jpa.PostRepository;
import com.vladmihalcea.spring.demo.repository.jpa.UserRepository;
import com.vladmihalcea.spring.demo.service.jpa.ForumService;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicJpaDemoTest extends BaseDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	public static final int POST_COUNT = 5;

	public static final int POST_COMMENT_COUNT = 10;

	@Autowired @Qualifier("JpaForumService")
	private ForumService forumService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private HyperUserRepository hyperUserRepository;

	@Autowired @Qualifier("JpaPostRepository")
	private PostRepository postRepository;

	@Autowired
	private PostCommentRepository postCommentRepository;

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
}
