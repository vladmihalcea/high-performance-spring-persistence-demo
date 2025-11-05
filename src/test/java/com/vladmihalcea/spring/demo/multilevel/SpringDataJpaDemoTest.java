package com.vladmihalcea.spring.demo.multilevel;

import com.vladmihalcea.spring.demo.domain.jpa.Post;
import com.vladmihalcea.spring.demo.domain.jpa.PostComment;
import com.vladmihalcea.spring.demo.service.jpa.ForumService;
import io.hypersistence.utils.common.ExceptionUtil;
import org.hibernate.loader.MultipleBagFetchException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SpringDataJpaDemoTest extends BaseMultilevelDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired @Qualifier("JpaForumService")
	private ForumService forumService;

	@Test
	public void testMultiLevelFetchingWithCartesianProduct() {
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
	public void testMultiLevelFetchingWithProgressiveJoinFetching() {
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
}
