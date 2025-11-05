package com.vladmihalcea.spring.demo.domain.jdbctemplate;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
public class PostTag {

    private Long postId;

    private Long tagId;

    private List<Tag> tags;

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
