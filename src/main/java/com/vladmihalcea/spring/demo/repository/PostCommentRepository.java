package com.vladmihalcea.spring.demo.repository;

import com.vladmihalcea.spring.demo.domain.PostComment;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
@Repository
public interface PostCommentRepository extends BaseJpaRepository<PostComment, Long>,
        JpaSpecificationExecutor<PostComment> {

    List<PostComment> findAllByPostId(Long postId);

    @Query("""
        select pc
        from PostComment pc
        left join fetch pc.votes v
        left join fetch v.user u
        join pc.post p
        where p.id between :minId and :maxId
        """)
    List<PostComment> findAllWithVotesByPostIds(@Param("minId") Long minId, @Param("maxId") Long maxId);
}
