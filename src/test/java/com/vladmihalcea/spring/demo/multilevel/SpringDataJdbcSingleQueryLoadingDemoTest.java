package com.vladmihalcea.spring.demo.multilevel;

import com.vladmihalcea.spring.demo.config.SpringDataJdbcSingleQueryLoadingConfiguration;
import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.Post;
import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.PostTag;
import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.UserVote;
import com.vladmihalcea.spring.demo.service.springjdbc_singlequeryloading.ForumService;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;

import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.*;

@Import(SpringDataJdbcSingleQueryLoadingConfiguration.class)
public class SpringDataJdbcSingleQueryLoadingDemoTest extends BaseMultilevelDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired @Qualifier("SpringJdbcSingleQueryLoadingForumService")
	private ForumService forumService;

	@Test
	public void testMultiLevelFetching() {
		addHierarchicalData();

		SQLStatementCountValidator.reset();
		long startNanos = System.nanoTime();
		/**
		 * @see org.springframework.data.jdbc.core.convert.SingleQueryFallbackDataAccessStrategy#entityQualifiesForSingleQueryLoading
		 * @see org.springframework.data.jdbc.core.convert.SingleQueryFallbackDataAccessStrategy#findAllById(Iterable, Class)
		 */
		List<Post> posts = forumService.findWithCommentsAndTagsByIds(1L, 50L);
		LOGGER.debug("Fetching 50 Posts with comments and tags took {} ms", elapsedMillis(startNanos));

		assertEquals(POST_COUNT, posts.size());

		for (Post post : posts) {
			assertEquals(POST_COMMENT_COUNT, post.getComments().size());

			assertEquals(TAG_COUNT, post.getTags().size());
			for (PostTag tag : post.getTags()) {
				assertNotNull(tag.getName());
			}

			for (var comment : post.getComments()) {
				assertFalse("Expected votes to be populated for comment " + comment.getId(),
						comment.getVotes().isEmpty());
				for (UserVote vote : comment.getVotes()) {
					assertNotNull(vote.getId());
				}
			}
		}

		/*
		 * Query breakdown:
		 *   1  single-query LEFT JOIN: posts + post_comments + post_tags
		 *   1  SELECT id, name FROM tags WHERE id IN (…)           (tag-name enrichment)
		 *   1  SELECT id, comment_id, score FROM user_votes WHERE comment_id IN (…)  (vote enrichment)
		 * ---
		 *   3  total
		 */
		SQLStatementCountValidator.assertSelectCount(3);
	}
}
