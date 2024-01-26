package com.oha.posting.repository;

import com.oha.posting.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends  JpaRepository<Post, Long>, PostRepositoryCustom {
}
