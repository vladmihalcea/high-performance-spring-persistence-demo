package com.vladmihalcea.spring.demo.domain.jpa.dtos;

/**
 * @author Vlad Mihalcea
 */
public record PostSummary(
    Long postId,
    String title,
    Integer commentCount
) {
}
