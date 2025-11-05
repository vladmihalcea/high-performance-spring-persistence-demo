package com.vladmihalcea.spring.demo.domain.jpa.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.vladmihalcea.spring.demo.domain.jpa.UserVote;

/**
 * @author Vlad Mihalcea
 */
@EntityView(UserVote.class)
public interface UserVoteView {
    @IdMapping
    Long getId();

    UserView getUser();

    int getScore();
}
