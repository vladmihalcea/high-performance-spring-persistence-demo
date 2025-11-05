package com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading;

import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Vlad Mihalcea
 */
@Table(name = "post_tags")
public class PostTag {

    private Long postId;

    private Long tagId;

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
}
