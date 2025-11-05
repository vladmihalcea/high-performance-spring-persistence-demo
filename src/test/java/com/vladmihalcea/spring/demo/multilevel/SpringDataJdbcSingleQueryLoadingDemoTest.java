package com.vladmihalcea.spring.demo.multilevel;

import com.vladmihalcea.spring.demo.config.SpringDataJdbcSingleQueryLoadingConfiguration;
import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.Post;
import com.vladmihalcea.spring.demo.repository.springjdbc_singlequeryloading.PostRepository;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;
import static org.junit.Assert.assertEquals;

@Import(SpringDataJdbcSingleQueryLoadingConfiguration.class)
public class SpringDataJdbcSingleQueryLoadingDemoTest extends BaseMultilevelDemoTest {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired @Qualifier("SpringDataJdbc_SingleQueryLoading_PostRepository")
	private PostRepository postRepository;

	@Test
	public void testMultiLevelFetching() {
		addHierarchicalData();

		long startNanos = System.nanoTime();
		/**
		 * @see org.springframework.data.jdbc.core.convert.SingleQueryFallbackDataAccessStrategy#entityQualifiesForSingleQueryLoading
		 * @see org.springframework.data.jdbc.core.convert.SingleQueryFallbackDataAccessStrategy#findAllById(Iterable, Class) 
		 */
		Iterable<Post> postIterable = postRepository.findAllById(
			LongStream.rangeClosed(1L, 50L).boxed().toList()
		);
		List<Post> posts = StreamSupport.stream(postIterable.spliterator(), false).toList();
		LOGGER.debug("Fetching 50 Posts with comments and tags took {} ms", elapsedMillis(startNanos));

		assertEquals(POST_COUNT, posts.size());

		for (Post post : posts) {
			assertEquals(POST_COMMENT_COUNT, post.getComments().size());

			assertEquals(TAG_COUNT, post.getTags().size());
		}
	}
}
