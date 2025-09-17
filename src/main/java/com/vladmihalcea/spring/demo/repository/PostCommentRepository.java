package com.vladmihalcea.spring.demo.repository;

import com.vladmihalcea.spring.demo.domain.PostComment;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository
public interface PostCommentRepository
    extends BaseJpaRepository<PostComment, Long>, JpaSpecificationExecutor<PostComment> {
}
