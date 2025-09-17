package com.vladmihalcea.spring.demo.repository;

import com.vladmihalcea.spring.demo.domain.User;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository
public interface HyperUserRepository extends BaseJpaRepository<User, String> {
}
