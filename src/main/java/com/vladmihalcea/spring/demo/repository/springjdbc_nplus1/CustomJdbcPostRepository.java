package com.vladmihalcea.spring.demo.repository.springjdbc_nplus1;

import com.vladmihalcea.spring.demo.domain.springjdbc_nplus1.Post;

import java.util.List;

/**
 * Custom repository fragment that enriches the standard Spring Data JDBC loading with
 * tag names resolved from the {@code tags} table in a single extra batch query.
 *
 * @author Vlad Mihalcea
 */
public interface CustomJdbcPostRepository {

    /**
     * Loads the same aggregate graph as {@code CrudRepository.findAllById} but
     * additionally populates {@link com.vladmihalcea.spring.demo.domain.springjdbc_nplus1.PostTag#getName()}
     * for every tag on every post via one batch {@code SELECT id, name FROM tags WHERE id IN (…)}.
     */
    List<Post> findAllByIdWithTagNames(List<Long> ids);
}

