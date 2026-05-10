package com.vladmihalcea.spring.demo.repository.jooq.schema;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.Date;

import static org.jooq.impl.DSL.*;

/**
 * Centralised jOOQ table and field constants for this project.
 * <p>
 * All constants are derived directly from the physical column names used by the
 * JPA entities and the existing jOOQ repositories so that they can be shared
 * across {@link com.vladmihalcea.spring.demo.repository.jooq.JooqPostRepository}
 * and {@link com.vladmihalcea.spring.demo.repository.jooq.JooqMultiQueryPostRepository}
 * without duplicating {@code field(name(…))} calls.
 *
 * @author Vlad Mihalcea
 */
public final class Tables {

    // -------------------------------------------------------------------------
    // Tables
    // -------------------------------------------------------------------------
    public static final Table<Record> POSTS         = table(name("posts"));
    public static final Table<Record> POST_COMMENTS = table(name("post_comments"));
    public static final Table<Record> USER_VOTES    = table(name("user_votes"));
    public static final Table<Record> USERS         = table(name("users"));
    public static final Table<Record> TAGS          = table(name("tags"));
    public static final Table<Record> POST_TAGS     = table(name("post_tags"));

    // -------------------------------------------------------------------------
    // posts columns
    // -------------------------------------------------------------------------
    public static final Field<Long>   POSTS_ID         = field(name("posts", "id"),         Long.class);
    public static final Field<String> POSTS_TITLE      = field(name("posts", "title"),      String.class);
    public static final Field<Date>   POSTS_CREATED_ON = field(name("posts", "created_on"), Date.class);

    // -------------------------------------------------------------------------
    // post_comments columns
    // -------------------------------------------------------------------------
    public static final Field<Long>   PC_ID      = field(name("post_comments", "id"),      Long.class);
    public static final Field<String> PC_REVIEW  = field(name("post_comments", "review"),  String.class);
    public static final Field<Long>   PC_POST_ID = field(name("post_comments", "post_id"), Long.class);

    // -------------------------------------------------------------------------
    // user_votes columns
    // -------------------------------------------------------------------------
    public static final Field<Long>    UV_ID         = field(name("user_votes", "id"),         Long.class);
    public static final Field<Integer> UV_SCORE      = field(name("user_votes", "score"),      Integer.class);
    public static final Field<Long>    UV_COMMENT_ID = field(name("user_votes", "comment_id"), Long.class);
    public static final Field<String>  UV_USER_ID    = field(name("user_votes", "user_id"),    String.class);

    // -------------------------------------------------------------------------
    // users columns  (PK is national_id – a natural string key)
    // -------------------------------------------------------------------------
    public static final Field<String> USERS_NATIONAL_ID = field(name("users", "national_id"), String.class);
    public static final Field<String> USERS_FIRST_NAME  = field(name("users", "first_name"),  String.class);
    public static final Field<String> USERS_LAST_NAME   = field(name("users", "last_name"),   String.class);

    // -------------------------------------------------------------------------
    // tags columns
    // -------------------------------------------------------------------------
    public static final Field<Long>   TAGS_ID   = field(name("tags", "id"),   Long.class);
    public static final Field<String> TAGS_NAME = field(name("tags", "name"), String.class);

    // -------------------------------------------------------------------------
    // post_tags columns  (pure join table – no surrogate key)
    // -------------------------------------------------------------------------
    public static final Field<Long> PT_POST_ID = field(name("post_tags", "post_id"), Long.class);
    public static final Field<Long> PT_TAG_ID  = field(name("post_tags", "tag_id"),  Long.class);

    private Tables() {}
}

