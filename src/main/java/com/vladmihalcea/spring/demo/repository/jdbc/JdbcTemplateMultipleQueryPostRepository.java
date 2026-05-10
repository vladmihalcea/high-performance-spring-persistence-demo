package com.vladmihalcea.spring.demo.repository.jdbc;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vlad Mihalcea
 */
@Component
public class JdbcTemplateMultipleQueryPostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateMultipleQueryPostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        //Query 1: Fetch Posts with their Tags
        Map<Long, Post> postMap = new LinkedHashMap<>();

        jdbcTemplate.query("""
                SELECT p.id, p.title, p.created_on,
                       t.id, t.name
                FROM posts p
                LEFT JOIN post_tags pt ON pt.post_id = p.id
                LEFT JOIN tags t ON pt.tag_id = t.id
                WHERE p.id BETWEEN ? AND ?
                """,
            rs -> {
                Long postId = rs.getLong(1);
                Post post = postMap.computeIfAbsent(postId, id -> {
                    try {
                        return new Post()
                            .setId(id)
                            .setTitle(rs.getString(2))
                            .setCreatedOn(rs.getDate(3));
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                });

                long tagId = rs.getLong(4);
                if (!rs.wasNull()) {
                    String tagName = rs.getString(5);
                    Tag tag = new Tag().setId(tagId).setName(tagName);
                    if (!post.getTags().contains(tag)) {
                        post.getTags().add(tag);
                    }
                }
            },
            minId, maxId
        );

        //Query 2: Fetch PostComments with UserVotes and User
        Map<Long, PostComment> commentMap = new LinkedHashMap<>();

        jdbcTemplate.query("""
                SELECT pc.id, pc.review, pc.post_id,
                       uv.id, uv.score,
                       u.national_id, u.first_name, u.last_name
                FROM post_comments pc
                LEFT JOIN user_votes uv ON uv.comment_id = pc.id
                LEFT JOIN users u ON uv.user_id = u.national_id
                WHERE pc.post_id BETWEEN ? AND ?
                """,
            rs -> {
                Long commentId = rs.getLong(1);
                PostComment comment = commentMap.computeIfAbsent(commentId, id -> {
                    try {
                        PostComment c = new PostComment()
                            .setId(id)
                            .setReview(rs.getString(2));
                        Long postId = rs.getLong(3);
                        Post post = postMap.get(postId);
                        if (post != null) {
                            post.getComments().add(c);
                        }
                        return c;
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                });

                long voteId = rs.getLong(4);
                if (!rs.wasNull()) {
                    UserVote vote = new UserVote()
                        .setId(voteId)
                        .setScore(rs.getInt(5))
                        .setUser(new User()
                            .setId(rs.getString(6))
                            .setFirstName(rs.getString(7))
                            .setLastName(rs.getString(8)));
                    comment.getVotes().add(vote);
                }
            },
            minId, maxId
        );

        return new ArrayList<>(postMap.values());
    }
}

