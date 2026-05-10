package com.vladmihalcea.spring.demo.service.jdbctemplate;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.Post;
import com.vladmihalcea.spring.demo.repository.jdbc.JdbcTemplateMultipleQueryPostRepository;
import com.vladmihalcea.spring.demo.service.PostHierarchyRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
@Service(value = "JdbcTemplateMultipleQueryForumService")
@Transactional(readOnly = true)
public class MultipleQueryForumService implements PostHierarchyRetrievalService<Post> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final JdbcTemplateMultipleQueryPostRepository postRepository;

    public MultipleQueryForumService(JdbcTemplateMultipleQueryPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        return postRepository.findWithCommentsAndTagsByIds(minId, maxId);
    }
}

