package com.vladmihalcea.spring.demo.repository.jooq;

import com.vladmihalcea.spring.demo.domain.jdbctemplate.*;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.vladmihalcea.spring.demo.repository.jooq.schema.Tables.*;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;

/**
 * @author Vlad Mihalcea
 */
@Component
public class JooqPostRepository {

    private final DSLContext dsl;

    public JooqPostRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        return dsl.select(
                POSTS_ID,
                POSTS_TITLE,
                multiset(
                    dsl.select(
                            PC_ID,
                            PC_REVIEW,
                            multiset(
                                dsl.select(UV_ID, UV_SCORE, USERS_NATIONAL_ID, USERS_FIRST_NAME, USERS_LAST_NAME)
                                    .from(USER_VOTES)
                                    .join(USERS).on(UV_USER_ID.eq(USERS_NATIONAL_ID))
                                    .where(UV_COMMENT_ID.eq(PC_ID))
                            ).convertFrom(result -> result.map(mapping((id, score, nationalId, firstName, lastName) ->
                                new UserVote()
                                    .setId(id)
                                    .setScore(score)
                                    .setUser(new User()
                                        .setId(nationalId)
                                        .setFirstName(firstName)
                                        .setLastName(lastName))
                            )))
                        )
                        .from(POST_COMMENTS)
                        .where(PC_POST_ID.eq(POSTS_ID))
                ).convertFrom(result -> result.map(mapping((id, review, votes) -> {
                    PostComment comment = new PostComment()
                        .setId(id)
                        .setReview(review);
                    comment.getVotes().addAll(votes);
                    return comment;
                }))),
                multiset(
                    dsl.select(TAGS_ID, TAGS_NAME)
                        .from(POST_TAGS)
                        .join(TAGS).on(PT_TAG_ID.eq(TAGS_ID))
                        .where(PT_POST_ID.eq(POSTS_ID))
                ).convertFrom(result -> result.map(mapping((id, tagName) -> new Tag()
                    .setId(id)
                    .setName(tagName)))
                )
            )
            .from(POSTS)
            .where(POSTS_ID.between(minId, maxId))
            .fetch(mapping((id, title, comments, tags) -> {
                Post post = new Post()
                    .setId(id)
                    .setTitle(title);
                post.getComments().addAll(comments);
                post.getTags().addAll(tags);
                return post;
            }));
    }
}