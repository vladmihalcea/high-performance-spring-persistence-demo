package com.vladmihalcea.spring.demo.domain.springjdbc_nplus1;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Vlad Mihalcea
 */
@Table(name = "user_votes")
public class UserVote {

    @Id
    private Long id;

    private int score;

    public Long getId() {
        return id;
    }

    public UserVote setId(Long id) {
        this.id = id;
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
