package com.oha.posting.repository;

import com.oha.posting.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    Optional<Comment> findByCommentIdAndIsDelAndIsParent(Long commentId, Boolean isDel, Boolean isParent);
}
