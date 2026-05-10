package com.vladmihalcea.spring.demo.repository.springjdbc_singlequeryloading;

import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.Post;

import java.util.List;

/**
 * Fragment interface that enriches the standard single-query loading with two
 * extra batch queries: one for tag names and one for user votes.
 *
 * @author Vlad Mihalcea
 */
public interface CustomJdbcSingleQueryLoadingPostRepository {

    /**
     * Loads posts (with comments and tags via single-query loading), then
     * enriches each {@link com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.PostTag}
     * with its tag name and each {@link com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.PostComment}
     * with its votes — both via single batch queries.
     */
    List<Post> findAllByIdWithTagNamesAndVotes(List<Long> ids);
}

