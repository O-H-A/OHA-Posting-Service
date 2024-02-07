package com.oha.posting.repository;

import com.oha.posting.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends  JpaRepository<Post, Long>, PostRepositoryCustom {

    Optional<Post> findByPostIdAndIsDel(Long postId, Boolean isDel);
}
