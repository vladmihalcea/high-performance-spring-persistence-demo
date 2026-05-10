package com.vladmihalcea.spring.demo.service.springjdbc_singlequeryloading;

import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.Post;
import com.vladmihalcea.spring.demo.repository.springjdbc_singlequeryloading.PostRepository;
import com.vladmihalcea.spring.demo.service.PostHierarchyRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.LongStream;

/**
 * @author Vlad Mihalcea
 */
@Service(value = "SpringJdbcSingleQueryLoadingForumService")
@Transactional(readOnly = true)
public class ForumService implements PostHierarchyRetrievalService<Post> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final PostRepository postRepository;

    public ForumService(@Qualifier("SpringDataJdbc_SingleQueryLoading_PostRepository") PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        return postRepository.findAllByIdWithTagNamesAndVotes(
            LongStream.rangeClosed(minId, maxId).boxed().toList()
        );
    }
}

