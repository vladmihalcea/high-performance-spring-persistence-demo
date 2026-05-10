package com.vladmihalcea.spring.demo.service.jdbctemplate;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.*;
import com.vladmihalcea.spring.demo.service.PostHierarchyRetrievalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vlad Mihalcea
 */
@Service(value = "JdbcTemplateCartesianProductForumService")
@Transactional(readOnly = true)
public class CartesianProductForumService implements PostHierarchyRetrievalService<Post> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbcTemplate;

    public CartesianProductForumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        List<Post> posts = jdbcTemplate.query("""
            SELECT p.id, p.title, p.created_on, pc.id, pc.review, uv.id, uv.score,
                   u.national_id, u.first_name, u.last_name, t.id, t.name
            FROM posts p
            JOIN post_comments pc ON pc.post_id = p.id
            JOIN user_votes uv ON uv.comment_id = pc.id
            JOIN users u ON uv.user_id = u.national_id
            JOIN post_tags pt ON pt.post_id = p.id
            JOIN tags t ON pt.tag_id = t.id
            WHERE p.id >= ? AND p.id <= ?
            ORDER BY p.id, pc.id, uv.id, u.national_id, t.id
            """,
            new Object[]{minId, maxId},
            new HierarchicalRowMapper()
        ).stream().distinct().toList();

        LOGGER.debug("Fetched {} posts along with all their comments", posts.size());

        return posts;
    }

    private static class HierarchicalRowMapper implements RowMapper<Post> {

        private Map<Long, Post> postMap = new LinkedHashMap<>();
        private Map<Long, PostComment> postCommentMap = new LinkedHashMap<>();
        private Map<Long, UserVote> userVoteMap = new LinkedHashMap<>();
        private Map<Long, Tag> tagMap = new LinkedHashMap<>();

        @Override
        public Post mapRow(ResultSet rs, int rowNum) {
            Long postId = PostHierarchyColumn.POST_ID.get(rs);

            Post post = postMap.computeIfAbsent(
                postId,
            id -> new Post()
                    .setId(id)
                    .setTitle(PostHierarchyColumn.POST_TITLE.get(rs))
                    .setCreatedOn(PostHierarchyColumn.POST_CREATED_ON.get(rs))
            );
            Long commentId = PostHierarchyColumn.POST_COMMENT_ID.get(rs);
            PostComment comment = postCommentMap.computeIfAbsent(
                commentId,
                id -> {
                    PostComment _comment = new PostComment()
                        .setId(id)
                        .setReview(PostHierarchyColumn.POST_COMMENT_REVIEW.get(rs));
                    post.getComments().add(_comment);
                    return _comment;
                }
            );

            Long userVoteId = PostHierarchyColumn.USER_VOTE_ID.get(rs);
            userVoteMap.computeIfAbsent(
                userVoteId,
                id -> {
                    UserVote _userVote = new UserVote()
                        .setId(id)
                        .setScore(PostHierarchyColumn.USER_VOTE_SCORE.get(rs))
                        .setUser(new User()
                            .setId(PostHierarchyColumn.USER_ID.get(rs))
                            .setFirstName(PostHierarchyColumn.USER_FIRST_NAME.get(rs))
                            .setLastName(PostHierarchyColumn.USER_LAST_NAME.get(rs))
                        );
                    comment.getVotes().add(_userVote);
                    return _userVote;
                }
            );

            Long tagId = PostHierarchyColumn.TAG_ID.get(rs);
            Tag tag = tagMap.computeIfAbsent(
                tagId,
                id -> new Tag()
                    .setId(id)
                    .setName(PostHierarchyColumn.TAG_NAME.get(rs))
            );
            if(!post.getTags().contains(tag)) {
                post.getTags().add(tag);
            }

            return post;
        }
    }

    public enum PostHierarchyColumn {
        POST_ID(Long.class),
        POST_TITLE(String.class),
        POST_CREATED_ON(Date.class),
        POST_COMMENT_ID(Long.class),
        POST_COMMENT_REVIEW(String.class),
        USER_VOTE_ID(Long.class),
        USER_VOTE_SCORE(Integer.class),
        USER_ID(String.class),
        USER_FIRST_NAME(String.class),
        USER_LAST_NAME(String.class),
        TAG_ID(Long.class),
        TAG_NAME(String.class);

        private final Class clazz;

        PostHierarchyColumn(Class clazz) {
            this.clazz = clazz;
        }

        public <T> T get(ResultSet rs) {
            try {
                return (T) rs.getObject(ordinal() + 1, clazz);
            } catch (SQLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
