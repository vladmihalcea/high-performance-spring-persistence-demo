package com.vladmihalcea.spring.demo.multilevel;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.Post;
import com.vladmihalcea.spring.demo.domain.jdbctemplate.PostComment;
import com.vladmihalcea.spring.demo.service.jdbctemplate.CartesianProductForumService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.assertEquals;

public class JdbcTemplateCartesianProductDemoTest extends BaseMultilevelDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired @Qualifier("JdbcTemplateCartesianProductForumService")
	private CartesianProductForumService forumService;

	@Test
	public void testMultiLevelFetching() {
		addHierarchicalData();

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
}
