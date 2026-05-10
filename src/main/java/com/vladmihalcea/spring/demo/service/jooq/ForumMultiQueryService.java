package com.vladmihalcea.spring.demo.service.jooq;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.Post;
import com.vladmihalcea.spring.demo.repository.jooq.JooqMultiQueryPostRepository;
import com.vladmihalcea.spring.demo.service.PostHierarchyRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ForumService backed by {@link JooqMultiQueryPostRepository}:
 * fetches Posts+Tags in one query and PostComments+UserVotes in a second query,
 * both expressed with the jOOQ DSL.
 *
 * @author Vlad Mihalcea
 */
@Service(value = "JooqMultiQueryForumService")
@Transactional(readOnly = true)
public class ForumMultiQueryService implements PostHierarchyRetrievalService<Post> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final JooqMultiQueryPostRepository postRepository;

    public ForumMultiQueryService(JooqMultiQueryPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        return postRepository.findWithCommentsAndTagsByIds(minId, maxId);
    }
}

