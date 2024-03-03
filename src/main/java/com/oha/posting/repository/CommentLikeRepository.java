package com.oha.posting.repository;

import com.oha.posting.entity.CommentLike;
import com.oha.posting.entity.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
}
