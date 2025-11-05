package com.vladmihalcea.spring.demo.domain.jdbctemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vlad Mihalcea
 */
public class PostComment {

    private Long id;

    private String review;

    private List<UserVote> votes = new ArrayList<>();;

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

    public List<UserVote> getVotes() {
        return votes;
    }

    public PostComment addVote(UserVote vote) {
        votes.add(vote);
        return this;
    }
}
