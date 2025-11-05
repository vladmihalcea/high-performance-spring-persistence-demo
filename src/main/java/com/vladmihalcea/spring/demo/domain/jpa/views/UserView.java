package com.vladmihalcea.spring.demo.domain.jpa.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.vladmihalcea.spring.demo.domain.jpa.User;

/**
 * @author Vlad Mihalcea
 */
@EntityView(User.class)
public interface UserView {
    @IdMapping
    String getId();

    String getFirstName();

    String getLastName();
}
