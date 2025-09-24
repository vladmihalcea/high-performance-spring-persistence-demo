package com.vladmihalcea.spring.demo.repository;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.vladmihalcea.spring.demo.domain.Post;
import com.vladmihalcea.spring.demo.domain.PostComment;
import com.vladmihalcea.spring.demo.domain.Post_;
import com.vladmihalcea.spring.demo.domain.views.PostWithCommentsAndTagsView;
import com.vladmihalcea.spring.demo.dtos.PostSummary;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
public class CustomPostRepositoryImpl implements CustomPostRepository {

    private final EntityManager entityManager;

    private final CriteriaBuilderFactory criteriaBuilderFactory;

    private final EntityViewManager entityViewManager;

    public CustomPostRepositoryImpl(
            EntityManager entityManager,
            CriteriaBuilderFactory criteriaBuilderFactory,
            EntityViewManager entityViewManager) {
        this.entityManager = entityManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
        this.entityViewManager = entityViewManager;
    }

    @Override
    public PagedList<Post> findTopN(Sort sortBy, int pageSize) {
        return sortedCriteriaBuilder(sortBy)
            .page(0, pageSize)
            .withKeysetExtraction(true)
            .getResultList();
    }

    @Override
    public PagedList<Post> findNextN(Sort sortBy, PagedList<Post> previousPage) {
        return sortedCriteriaBuilder(sortBy)
            .page(
                previousPage.getKeysetPage(),
                previousPage.getPage() * previousPage.getMaxResults(),
                previousPage.getMaxResults()
            )
            .getResultList();
    }

    @Override
    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        List<Post> posts = entityManager.createQuery("""
                select p
                from Post p
                left join fetch p.comments
                where p.id between :minId and :maxId
                """, Post.class)
            .setParameter("minId", minId)
            .setParameter("maxId", maxId)
            .getResultList();

        entityManager.createQuery("""
                select p
                from Post p
                left join fetch p.tags t
                where p.id between :minId and :maxId
                """, Post.class)
            .setParameter("minId", minId)
            .setParameter("maxId", maxId)
            .getResultList();

        entityManager.createQuery("""
                select pc
                from PostComment pc
                left join fetch pc.votes v
                left join fetch v.user u
                join pc.post p
                where p.id between :minId and :maxId
                """, PostComment.class)
            .setParameter("minId", minId)
            .setParameter("maxId", maxId)
            .getResultList();

        return posts;
    }

    @Override
    public List<PostWithCommentsAndTagsView> findPostWithCommentsAndTagsViewByIds(
            Long minId, Long maxId) {
        return entityViewManager.applySetting(
            EntityViewSetting.create(PostWithCommentsAndTagsView.class),
            criteriaBuilderFactory.create(entityManager, Post.class)
        )
        .where(Post_.ID)
            .betweenExpression(":minId")
            .andExpression(":maxId")
        .setParameter("minId", minId)
        .setParameter("maxId", maxId)
        .getResultList();
    }

    @Override
    public List<PostSummary> firstLatestPostSummaries(int pageSize) {
        return entityManager.createQuery("""
            select 
                p.id,
                p.title,
                size(p.comments)
            from Post p
            order by p.createdOn desc, p.id desc
            """, PostSummary.class)
        .setMaxResults(pageSize)
        .getResultList();
    }

    private CriteriaBuilder<Post> sortedCriteriaBuilder(Sort sortBy) {
        CriteriaBuilder<Post> criteriaBuilder = criteriaBuilderFactory
            .create(entityManager, Post.class);
        sortBy.forEach(order -> {
            criteriaBuilder.orderBy(order.getProperty(), order.isAscending());
        });
        return criteriaBuilder;
    }
}
