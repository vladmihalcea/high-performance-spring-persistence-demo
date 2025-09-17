package com.vladmihalcea.spring.demo.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vlad Mihalcea
 */
@Entity
@Table(
    name = "post_comments",
    indexes = @Index(
        name = "FK_post_comment_post_id",
        columnList = "post_id"
    )
)
public class PostComment {

    @Id
    @GeneratedValue
    private Long id;

    private String review;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVote> votes = new ArrayList<>();

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

    public Post getPost() {
        return post;
    }

    public PostComment setPost(Post post) {
        this.post = post;
        return this;
    }

    public List<UserVote> getVotes() {
        return votes;
    }

    public PostComment addVote(UserVote vote) {
        votes.add(vote);
        vote.setComment(this);
        return this;
    }
}
