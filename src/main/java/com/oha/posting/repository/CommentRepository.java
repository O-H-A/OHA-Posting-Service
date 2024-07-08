package com.oha.posting.repository;

import com.oha.posting.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    Optional<Comment> findByCommentIdAndIsDelAndType(Long commentId, Boolean isDel, String type);
    Optional<Comment> findByCommentIdAndIsDelAndTypeIn(Long commentId, Boolean isDel, List<String> type);

    Optional<Comment> findByCommentIdAndIsDel(Long commentId, boolean b);

    @Query("SELECT p.postId, count(c.commentId) FROM Post p LEFT JOIN p.comments c ON not c.isDel WHERE p.postId IN (:ids) GROUP BY p.postId")
    List<Object[]> findPostCommentCountByIds(@Param("ids") List<Long> ids);
}
