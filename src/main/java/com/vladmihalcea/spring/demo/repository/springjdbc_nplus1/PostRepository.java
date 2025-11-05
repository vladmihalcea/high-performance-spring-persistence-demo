package com.vladmihalcea.spring.demo.repository.springjdbc_nplus1;

import com.vladmihalcea.spring.demo.domain.springjdbc_nplus1.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository(value = "SpringDataJdbc_NPlus1_PostRepository")
public interface PostRepository extends CrudRepository<Post, Long> {

}
