package com.vladmihalcea.spring.demo.service.jdbctemplate;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.vladmihalcea.spring.util.Utils.uncheck;

/**
 * @author Vlad Mihalcea
 */
@Service(value = "JdbcTemplateForumService")
@Transactional(readOnly = true)
public class ForumService {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbcTemplate;

    public ForumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        var postMap = new LinkedHashMap<Long, Post>();
        var commentMap = new LinkedHashMap<Long, PostComment>();
        var tagMap = new LinkedHashMap<Long, Tag>();
        var userMap = new LinkedHashMap<String, User>();

        jdbcTemplate.query("""
                SELECT p.id, p.title, p.created_on, pc.id, pc.review
                FROM posts p
                LEFT JOIN post_comments pc ON pc.post_id = p.id
                WHERE p.id >= ? AND p.id <= ?
                ORDER BY p.id, pc.id
                """,
            rs -> {
                var postId = rs.getObject(1, Long.class);
                var post = postMap.computeIfAbsent(postId, id -> new Post()
                    .setId(id)
                    .setTitle(uncheck(() -> rs.getString(2)))
                    .setCreatedOn(uncheck(() -> rs.getObject(3, Date.class)))
                );
                var commentId = rs.getObject(4, Long.class);
                if (commentId != null) {
                    var comment = new PostComment()
                        .setId(commentId)
                        .setReview(rs.getString(5));
                    commentMap.put(commentId, comment);
                    post.addComment(comment);
                }
            },
            minId, maxId
        );

        jdbcTemplate.query("""
                SELECT p.id, t.id, t.name
                FROM posts p
                LEFT JOIN post_tags pt ON pt.post_id = p.id
                LEFT JOIN tags t ON pt.tag_id  = t.id
                WHERE p.id >= ? AND p.id <= ?
                ORDER BY p.id, t.id
                """,
            rs -> {
                var post = postMap.get(rs.getObject(1, Long.class));
                var tagId = rs.getObject(2, Long.class);
                if (tagId != null) {
                    var tag = tagMap.computeIfAbsent(tagId, id ->
                        new Tag()
                            .setId(id)
                            .setName(uncheck(() -> rs.getString(3)))
                    );
                    post.addTag(tag);
                }
            },
            minId, maxId
        );

        jdbcTemplate.query("""
                SELECT pc.id, uv.id, uv.score,
                    u.national_id, u.first_name, u.last_name
                FROM post_comments pc
                JOIN user_votes uv ON uv.comment_id = pc.id
                JOIN users u ON uv.user_id = u.national_id
                WHERE pc.post_id >= ? AND pc.post_id <= ?
                ORDER BY pc.id, uv.id
                """,
            rs -> {
                var commentId = rs.getObject(1, Long.class);
                var comment = commentMap.get(commentId);
                var userId = rs.getString(4);
                var user = userMap.computeIfAbsent(userId, id -> new User()
                    .setId(id)
                    .setFirstName(uncheck(() -> rs.getString(5)))
                    .setLastName(uncheck(() -> rs.getString(6))));
                comment.addVote(new UserVote()
                    .setId(rs.getObject(2, Long.class))
                    .setScore(rs.getInt(3))
                    .setUser(user)
                );
            },
            minId, maxId
        );

        LOGGER.debug("Fetched {} posts along with all their comments", postMap.size());
        return new ArrayList<>(postMap.values());
    }
}
