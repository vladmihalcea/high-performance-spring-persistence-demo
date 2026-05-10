package com.vladmihalcea.spring.demo.multilevel;

import com.vladmihalcea.spring.demo.domain.springjdbc_nplus1.Post;
import com.vladmihalcea.spring.demo.domain.springjdbc_nplus1.PostTag;
import com.vladmihalcea.spring.demo.service.springjdbc_nplus1.ForumService;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpringDataJdbcDefaultDemoTest extends BaseMultilevelDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired @Qualifier("SpringJdbcNPlus1ForumService")
	private ForumService forumService;

	@Test
	public void testMultiLevelFetching() {
		addHierarchicalData();

		SQLStatementCountValidator.reset();
		long startNanos = System.nanoTime();
		List<Post> posts = forumService.findWithCommentsAndTagsByIds(1L, 50L);
		LOGGER.debug("Fetching 50 Posts with comments and tags took {} ms", elapsedMillis(startNanos));

		assertEquals(POST_COUNT, posts.size());

		for (Post post : posts) {
			assertEquals(POST_COMMENT_COUNT, post.getComments().size());

			assertEquals(TAG_COUNT, post.getTags().size());
			for (PostTag tag : post.getTags()) {
				assertNotNull(tag.getName());
			}
		}
		/*
		 * N+1 queries from Spring Data JDBC default loading strategy:
		 *    1  SELECT posts WHERE id IN (1..50)
		 *   50  SELECT post_comments WHERE post_id = ?    (one per post)
		 *   50  SELECT post_tags WHERE post_id = ?        (one per post)
		 * 1000  SELECT user_votes WHERE comment_id = ?    (one per comment)
		 * ----
		 * 1101  sub-total (N+1 query issue)
		 *    1  SELECT id, name FROM tags WHERE id IN (…) (batch tag-name enrichment)
		 * ----
		 * 1102  total
		 */
		SQLStatementCountValidator.assertSelectCount(1102);
	}
}
