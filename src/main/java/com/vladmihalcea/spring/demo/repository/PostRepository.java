package com.vladmihalcea.spring.demo.repository;

import com.vladmihalcea.spring.demo.domain.Post;
import com.vladmihalcea.spring.demo.dtos.PostSummary;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {

    @Query("""
        select 
            p.id,
            p.title,
            size(p.comments)
        from Post p
        """)
    Page<PostSummary> firstLatestPostSummariesPage(Pageable pageRequest);
}
