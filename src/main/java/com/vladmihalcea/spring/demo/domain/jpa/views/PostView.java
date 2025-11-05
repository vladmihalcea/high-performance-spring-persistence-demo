package com.vladmihalcea.spring.demo.domain.jpa.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.vladmihalcea.spring.demo.domain.jpa.Post;

/**
 * @author Vlad Mihalcea
 */
@EntityView(Post.class)
public interface PostView {
    @IdMapping
    Long getId();

    String getTitle();
}
