package com.vladmihalcea.spring.demo.multilevel;

import com.vladmihalcea.spring.demo.domain.jpa.views.PostCommentView;
import com.vladmihalcea.spring.demo.domain.jpa.views.PostWithCommentsAndTagsView;
import com.vladmihalcea.spring.demo.service.jpa.ForumService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.assertEquals;

public class BlazePersistenceDemoTest extends BaseMultilevelDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired @Qualifier("JpaForumService")
	private ForumService forumService;

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
}
