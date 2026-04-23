package com.vladmihalcea.spring.demo.domain.jdbctemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Vlad Mihalcea
 */
public class Post {

    private Long id;

    private String title;

    private Date createdOn;

    private List<PostComment> comments = new ArrayList<>();

    private List<Tag> tags = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Post setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Post setTitle(String title) {
        this.title = title;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public Post setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public List<PostComment> getComments() {
        return comments;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Post addComment(PostComment comment) {
        comments.add(comment);
        return this;
    }

    public Post addTag(Tag tag) {
        tags.add(tag);
        return this;
    }
}
