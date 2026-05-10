package com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collections;
import java.util.Set;

/**
 * @author Vlad Mihalcea
 */
@Table(name = "post_comments")
public class PostComment {

    @Id
    private Long id;

    private String review;

    /**
     * Nested collections are not supported by single-query loading.
     * Populated by CustomPostRepositoryImpl via a batch query on user_votes.
     */
    @Transient
    private Set<UserVote> votes = Collections.emptySet();

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

    public Set<UserVote> getVotes() {
        return votes;
    }

    public void setVotes(Set<UserVote> votes) {
        this.votes = votes;
    }
}
