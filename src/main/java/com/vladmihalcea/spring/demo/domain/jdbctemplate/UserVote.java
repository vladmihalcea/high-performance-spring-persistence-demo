package com.vladmihalcea.spring.demo.domain.jdbctemplate;

/**
 * @author Vlad Mihalcea
 */
public class UserVote {

    private Long id;

    private User user;

    private int score;

    public Long getId() {
        return id;
    }

    public UserVote setId(Long id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public UserVote setUser(User user) {
        this.user = user;
        return this;
    }

    public int getScore() {
        return score;
    }

    public UserVote setScore(int score) {
        this.score = score;
        return this;
    }
}
