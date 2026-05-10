package com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author Vlad Mihalcea
 */
@Table(name = "post_tags")
public class PostTag {

    private Long postId;

    private Long tagId;

    /**
     * Not a column in {@code post_tags} – populated by
     * CustomPostRepositoryImpl via a single batch query against the {@code tags} table.
     */
    @Transient
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
