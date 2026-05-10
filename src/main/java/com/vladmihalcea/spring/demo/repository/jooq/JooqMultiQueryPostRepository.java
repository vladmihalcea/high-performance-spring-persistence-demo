package com.vladmihalcea.spring.demo.repository.jooq;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.*;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vladmihalcea.spring.demo.repository.jooq.schema.Tables.*;

/**
 * jOOQ-based multi-query post repository.
 * Uses two separate queries instead of MULTISET correlated sub-selects:
 * <ol>
 *   <li>Fetch Posts with their Tags via a LEFT JOIN</li>
 *   <li>Fetch PostComments with UserVotes and User via a LEFT JOIN</li>
 * </ol>
 *
 * @author Vlad Mihalcea
 */
@Component
public class JooqMultiQueryPostRepository {

    private final DSLContext dsl;

    public JooqMultiQueryPostRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        // -----------------------------------------------------------------------
        // Query 1 – Posts LEFT JOIN post_tags LEFT JOIN tags
        // -----------------------------------------------------------------------
        Map<Long, Post> postMap = new LinkedHashMap<>();

        dsl.select(POSTS_ID, POSTS_TITLE, TAGS_ID, TAGS_NAME)
            .from(POSTS)
            .leftJoin(POST_TAGS).on(PT_POST_ID.eq(POSTS_ID))
            .leftJoin(TAGS).on(PT_TAG_ID.eq(TAGS_ID))
            .where(POSTS_ID.between(minId, maxId))
            .orderBy(POSTS_ID)
            .forEach(r -> {
                Long postId = r.get(POSTS_ID);
                Post post = postMap.computeIfAbsent(postId, id ->
                    new Post().setId(id).setTitle(r.get(POSTS_TITLE))
                );

                Long tagId = r.get(TAGS_ID);
                if (tagId != null) {
                    Tag tag = new Tag().setId(tagId).setName(r.get(TAGS_NAME));
                    if (!post.getTags().contains(tag)) {
                        post.getTags().add(tag);
                    }
                }
            });

        // -----------------------------------------------------------------------
        // Query 2 – PostComments LEFT JOIN user_votes LEFT JOIN users
        // -----------------------------------------------------------------------
        Map<Long, PostComment> commentMap = new LinkedHashMap<>();

        dsl.select(PC_ID, PC_REVIEW, PC_POST_ID,
                   UV_ID, UV_SCORE,
                   USERS_NATIONAL_ID, USERS_FIRST_NAME, USERS_LAST_NAME)
            .from(POST_COMMENTS)
            .leftJoin(USER_VOTES).on(UV_COMMENT_ID.eq(PC_ID))
            .leftJoin(USERS).on(UV_USER_ID.eq(USERS_NATIONAL_ID))
            .where(PC_POST_ID.between(minId, maxId))
            .orderBy(PC_ID)
            .forEach(r -> {
                Long commentId = r.get(PC_ID);
                PostComment comment = commentMap.computeIfAbsent(commentId, id -> {
                    PostComment c = new PostComment()
                        .setId(id)
                        .setReview(r.get(PC_REVIEW));
                    Post post = postMap.get(r.get(PC_POST_ID));
                    if (post != null) {
                        post.getComments().add(c);
                    }
                    return c;
                });

                Long voteId = r.get(UV_ID);
                if (voteId != null) {
                    UserVote vote = new UserVote()
                        .setId(voteId)
                        .setScore(r.get(UV_SCORE))
                        .setUser(new User()
                            .setId(r.get(USERS_NATIONAL_ID))
                            .setFirstName(r.get(USERS_FIRST_NAME))
                            .setLastName(r.get(USERS_LAST_NAME)));
                    comment.getVotes().add(vote);
                }
            });

        return new ArrayList<>(postMap.values());
    }
}

