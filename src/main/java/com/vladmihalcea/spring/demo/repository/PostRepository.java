package com.vladmihalcea.spring.demo.repository;

import com.vladmihalcea.spring.demo.domain.Post;
import com.vladmihalcea.spring.demo.domain.Post_;
import com.vladmihalcea.spring.demo.dtos.PostSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {

    @Query("""
        select p
        from Post p
        join fetch p.comments
        where p.id = :postId
        """)
    Post findByIdWithComments(@Param("postId") Long postId);

    @Query("""
        select 
            p.id,
            p.title,
            size(p.comments)
        from Post p
        """)
    Page<PostSummary> firstLatestPostSummariesPage(Pageable pageRequest);

    @Query("""
        select p
        from Post p
        left join fetch p.comments
        where p.id between :minId and :maxId
        """)
    List<Post> findAllWithCommentsByIds(@Param("minId") Long minId, @Param("maxId") Long maxId);

    @Query("""
        select p
        from Post p
        left join fetch p.tags
        where p.id between :minId and :maxId
        """)
    List<Post> findAllWithTagsByIds(@Param("minId") Long minId, @Param("maxId") Long maxId);

    default List<PostSummary> firstLatestPostSummariesAntiPattern(int pageSize) {
        return findAll(
            PageRequest.of(0, pageSize, Sort.by(List.of(
                Sort.Order.desc(Post_.CREATED_ON),
                Sort.Order.desc(Post_.ID)))
            ))
        .stream()
        .map(p -> new PostSummary(p.getId(), p.getTitle(), p.getComments().size()))
        .toList();
    }
}
