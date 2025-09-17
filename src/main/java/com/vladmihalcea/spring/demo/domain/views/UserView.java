package com.vladmihalcea.spring.demo.domain.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.vladmihalcea.spring.demo.domain.User;

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
