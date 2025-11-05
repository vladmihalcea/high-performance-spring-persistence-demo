package com.vladmihalcea.spring.demo.domain.springjdbc_nplus1;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;
import java.util.Set;

/**
 * @author Vlad Mihalcea
 */
@Table(name = "posts")
public class Post {

    @Id
    private Long id;

    private String title;

    @Column("created_on")
    private Date createdOn;

    @MappedCollection(idColumn = "post_id")
    private Set<PostComment> comments;

    @MappedCollection(idColumn = "post_id")
    private Set<PostTag> tags;

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

    public Set<PostComment> getComments() {
        return comments;
    }

    public Post addComment(PostComment comment) {
        comments.add(comment);
        return this;
    }

    public Set<PostTag> getTags() {
        return tags;
    }
}
