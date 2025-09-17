package com.vladmihalcea.spring.demo.repository;

import com.vladmihalcea.spring.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
