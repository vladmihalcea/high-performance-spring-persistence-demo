package com.vladmihalcea.spring.demo.dtos;

/**
 * @author Vlad Mihalcea
 */
public record PostSummary(
    Long postId,
    String title,
    Integer commentCount
) {
}
