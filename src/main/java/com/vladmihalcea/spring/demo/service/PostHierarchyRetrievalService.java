package com.vladmihalcea.spring.demo.service;

import java.util.List;

/**
 * @author Vlad Mihalcea
 */
public interface PostHierarchyRetrievalService<T> {

    List<T> findWithCommentsAndTagsByIds(Long minId, Long maxId);
}
