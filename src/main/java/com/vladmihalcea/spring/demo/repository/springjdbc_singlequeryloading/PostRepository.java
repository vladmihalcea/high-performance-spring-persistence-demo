package com.vladmihalcea.spring.demo.repository.springjdbc_singlequeryloading;

import com.vladmihalcea.spring.demo.domain.springjdbc_singlequeryloading.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Vlad Mihalcea
 */
@Repository(value = "SpringDataJdbc_SingleQueryLoading_PostRepository")
public interface PostRepository extends CrudRepository<Post, Long> {

}
