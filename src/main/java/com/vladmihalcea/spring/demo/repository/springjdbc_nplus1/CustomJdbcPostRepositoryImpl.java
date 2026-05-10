package com.vladmihalcea.spring.demo.repository.springjdbc_nplus1;

import com.vladmihalcea.spring.demo.domain.springjdbc_nplus1.Post;
import com.vladmihalcea.spring.demo.domain.springjdbc_nplus1.PostTag;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Fragment implementation that augments Spring Data JDBC's default N+1 loading
 * by resolving tag names in a single extra batch query.
 *
 * <p>Query breakdown for {@link #findAllByIdWithTagNames}:
 * <ol>
 *   <li>Standard Spring Data JDBC queries (same as {@code findAllById})</li>
 *   <li>One extra {@code SELECT id, name FROM tags WHERE id IN (…)} for all unique tag IDs</li>
 * </ol>
 *
 * @author Vlad Mihalcea
 */
public class CustomJdbcPostRepositoryImpl implements CustomJdbcPostRepository {

    private final JdbcAggregateOperations jdbcAggregateOperations;

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    public CustomJdbcPostRepositoryImpl(
            JdbcAggregateOperations jdbcAggregateOperations,
            NamedParameterJdbcOperations namedParameterJdbcOperations) {
        this.jdbcAggregateOperations = jdbcAggregateOperations;
        this.namedParameterJdbcOperations = namedParameterJdbcOperations;
    }

    @Override
    public List<Post> findAllByIdWithTagNames(List<Long> ids) {
        // Standard Spring Data JDBC loading – same queries as CrudRepository.findAllById
        List<Post> posts = StreamSupport.stream(
            jdbcAggregateOperations.findAllById(ids, Post.class).spliterator(), false
        ).toList();

        // Collect every unique tag ID referenced across all loaded posts
        List<Long> tagIds = posts.stream()
            .flatMap(post -> post.getTags().stream().map(PostTag::getTagId))
            .distinct()
            .toList();

        if (!tagIds.isEmpty()) {
            // ONE extra query – batch-load tag names for all unique tag IDs
            Map<Long, String> tagNameById = namedParameterJdbcOperations.query(
                "SELECT id, name FROM tags WHERE id IN (:ids)",
                Map.of("ids", tagIds),
                (rs, rowNum) -> Map.entry(rs.getLong("id"), rs.getString("name"))
            ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // Enrich every PostTag with its resolved name
            posts.forEach(post ->
                post.getTags().forEach(tag -> tag.setName(tagNameById.get(tag.getTagId())))
            );
        }

        return posts;
    }
}

