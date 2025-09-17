package com.vladmihalcea.spring.demo.service;

import com.blazebit.persistence.PagedList;
import com.vladmihalcea.spring.demo.domain.Post;
import com.vladmihalcea.spring.demo.domain.PostComment;
import com.vladmihalcea.spring.demo.domain.Post_;
import com.vladmihalcea.spring.demo.domain.views.PostWithCommentsAndTagsView;
import com.vladmihalcea.spring.demo.repository.PostCommentRepository;
import com.vladmihalcea.spring.demo.repository.PostRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.vladmihalcea.spring.util.Utils.elapsedMillis;

/**
 * @author Vlad Mihalcea
 */
@Service
@Transactional(readOnly = true)
public class ForumService {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final PostRepository postRepository;

    private final PostCommentRepository postCommentRepository;

    private final EntityManager entityManager;

    public ForumService(
            PostRepository postRepository,
            PostCommentRepository postCommentRepository,
            EntityManager entityManager) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void setPostTitle(Long postId, String title) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.setTitle(title);

        long saveStartNanos = System.nanoTime();
        postRepository.save(post);
        LOGGER.debug("Save call took {} ms", elapsedMillis(saveStartNanos));
    }

    @Transactional
    public void addPostCommentAntiPattern(Long postId, PostComment comment) {
        Post post = postRepository.findByIdWithComments(postId);
        post.addComment(comment);

        long saveStartNanos = System.nanoTime();
        postRepository.save(post);
        LOGGER.debug("Save call took {} ms", elapsedMillis(saveStartNanos));
    }

    @Transactional
    public void addPostComment(Long postId, PostComment comment) {
        entityManager.persist(
            comment.setPost(
                entityManager.getReference(Post.class, postId)
            )
        );
    }

    public PagedList<Post> firstLatestPosts(int pageSize) {
        return postRepository.findTopN(
            Sort.by(Post_.CREATED_ON).descending().and(Sort.by(Post_.ID).descending()),
            pageSize
        );
    }

    public PagedList<Post> findNextLatestPosts(PagedList<Post> previousPage) {
        return postRepository.findNextN(
            Sort.by(Post_.CREATED_ON).descending().and(Sort.by(Post_.ID).descending()),
            previousPage
        );
    }

    @Transactional(readOnly = true)
    public List<Post> findWithCommentsAndTagsByIds(Long minId, Long maxId) {
        List<Post> posts = postRepository.findAllWithCommentsByIds(minId, maxId);
        LOGGER.debug("Fetched {} posts along with all their comments", posts.size());

        posts = postRepository.findAllWithTagsByIds(minId, maxId);
        LOGGER.debug("Fetched {} posts along with all their tags", posts.size());

        List<PostComment> comments = postCommentRepository.findAllWithVotesByPostIds(minId, maxId);
        LOGGER.debug("Fetched {} comments along with all their user votes", comments.size());

        return posts;
    }

    public List<PostWithCommentsAndTagsView> findPostWithCommentsAndTagsViewByIds(Long minId, Long maxId) {
        return postRepository.findPostWithCommentsAndTagsViewByIds(minId, maxId);
    }
}
