package com.vladmihalcea.spring.demo.domain.springjdbc_nplus1;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

/**
 * @author Vlad Mihalcea
 */
@Table(name = "post_comments")
public class PostComment {

    @Id
    private Long id;

    private String review;

    @MappedCollection(idColumn = "comment_id")
    private Set<UserVote> votes;

    public Long getId() {
        return id;
    }

    public PostComment setId(Long id) {
        this.id = id;
        return this;
    }

    public String getReview() {
        return review;
    }

    public PostComment setReview(String review) {
        this.review = review;
        return this;
    }
}
