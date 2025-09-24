package com.vladmihalcea.spring.demo.repository;

import com.blazebit.persistence.PagedList;
import com.vladmihalcea.spring.demo.domain.Post;
import com.vladmihalcea.spring.demo.domain.views.PostWithCommentsAndTagsView;
import com.vladmihalcea.spring.demo.dtos.PostSummary;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
public interface CustomPostRepository {

    PagedList<Post> findTopN(Sort sortBy, int pageSize);

    PagedList<Post> findNextN(Sort sortBy, PagedList<Post> previousPage);

    List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId);

    List<PostWithCommentsAndTagsView> findPostWithCommentsAndTagsViewByIds(Long minId, Long maxId);

    List<PostSummary> firstLatestPostSummaries(int pageSize);
}
