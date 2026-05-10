package com.vladmihalcea.spring.demo.repository.springjdbc_singlequeryloading;

import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.Post;
import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.PostTag;
import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.UserVote;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Fragment implementation that:
 * <ol>
 *   <li>Loads posts + comments + tags in a <em>single query</em> via
 *       {@link JdbcAggregateOperations#findAllById} (backed by {@code setSingleQueryLoadingEnabled}).</li>
 *   <li>Batch-loads tag names: {@code SELECT id, name FROM tags WHERE id IN (…)}</li>
 *   <li>Batch-loads user votes: {@code SELECT id, comment_id, score FROM user_votes WHERE comment_id IN (…)}</li>
 * </ol>
 * Total: <strong>3 queries</strong> regardless of the number of posts.
 *
 * @author Vlad Mihalcea
 */
@Component
public class CustomJdbcSingleQueryLoadingPostRepositoryImpl implements CustomJdbcSingleQueryLoadingPostRepository {

    private final JdbcAggregateOperations jdbcAggregateOperations;

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    public CustomJdbcSingleQueryLoadingPostRepositoryImpl(
            JdbcAggregateOperations jdbcAggregateOperations,
            NamedParameterJdbcOperations namedParameterJdbcOperations) {
        this.jdbcAggregateOperations = jdbcAggregateOperations;
        this.namedParameterJdbcOperations = namedParameterJdbcOperations;
    }

    @Override
    public List<Post> findAllByIdWithTagNamesAndVotes(List<Long> ids) {
        // Query 1 — single-query loading for posts + post_comments + post_tags
        List<Post> posts = StreamSupport.stream(
            jdbcAggregateOperations.findAllById(ids, Post.class).spliterator(), false
        ).toList();

        addTagNames(posts);
        addCommentVotes(posts);

        return posts;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Query 2 — batch-load tag names for all unique tag IDs referenced by the posts.
     */
    private void addTagNames(List<Post> posts) {
        List<Long> tagIds = posts.stream()
            .flatMap(post -> post.getTags().stream().map(PostTag::getTagId))
            .distinct()
            .toList();

        if (tagIds.isEmpty()) {
            return;
        }

        Map<Long, String> tagNameById = namedParameterJdbcOperations.query(
            "SELECT id, name FROM tags WHERE id IN (:ids)",
            Map.of("ids", tagIds),
            (rs, rowNum) -> Map.entry(rs.getLong("id"), rs.getString("name"))
        ).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        posts.forEach(post ->
            post.getTags().forEach(tag -> tag.setName(tagNameById.get(tag.getTagId())))
        );
    }

    /**
     * Query 3 — batch-load all user votes for all comments across the fetched posts.
     * Groups the votes by {@code comment_id} and sets them on each {@link com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.PostComment}.
     */
    private void addCommentVotes(List<Post> posts) {
        List<Long> commentIds = posts.stream()
            .flatMap(post -> post.getComments().stream())
            .map(comment -> comment.getId())
            .toList();

        if (commentIds.isEmpty()) {
            return;
        }

        Map<Long, Set<UserVote>> votesByCommentId = namedParameterJdbcOperations.query(
            "SELECT id, comment_id, score FROM user_votes WHERE comment_id IN (:ids)",
            Map.of("ids", commentIds),
            (rs, rowNum) -> {
                UserVote vote = new UserVote()
                    .setId(rs.getLong("id"))
                    .setCommentId(rs.getLong("comment_id"))
                    .setScore(rs.getInt("score"));
                return vote;
            }
        ).stream().collect(Collectors.groupingBy(UserVote::getCommentId, Collectors.toSet()));

        posts.forEach(post ->
            post.getComments().forEach(comment ->
                comment.setVotes(
                    votesByCommentId.getOrDefault(comment.getId(), Collections.emptySet())
                )
            )
        );
    }
}

