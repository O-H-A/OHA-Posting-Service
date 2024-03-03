package com.oha.posting.repository;

import com.oha.posting.dto.comment.CommentSearchResponse;
import com.oha.posting.entity.CommentLikeId;
import com.oha.posting.entity.QComment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.oha.posting.entity.QComment.comment;
import static com.oha.posting.entity.QCommentLike.commentLike;
import static com.oha.posting.entity.QPost.post;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.list;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentSearchResponse> searchCommentList(BooleanBuilder builder, List<OrderSpecifier<?>> orderSpecifiers, int offset, int size) {
        QComment childComment = new QComment("childComment");
        return queryFactory
                .select(Projections.constructor(CommentSearchResponse.class
                        , comment.commentId
                        , comment.parent.commentId
                        , comment.post.postId
                        , comment.content
                        , comment.userId
                        , comment.taggedUserId
                        , comment.regDtm
                        , comment.updDtm
                        , childComment.count()
                ))
                .from(comment)
                .innerJoin(post)
                .on(comment.post.postId.eq(post.postId).and(post.isDel.eq(false)))
                .leftJoin(comment.child, childComment)
                .where(builder)
                .groupBy(comment.commentId, comment.post.postId, comment.content, comment.userId, comment.taggedUserId, comment.regDtm)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(offset)
                .limit(size)
                .fetch();
    }

    @Override
    public Map<Long, List<Long>> getCommentLikeUsers(List<Long> commentIds) {
        return queryFactory
                .select(Projections.constructor(CommentLikeId.class
                        , commentLike.commentLikeId.commentId
                        , commentLike.commentLikeId.userId))
                .from(commentLike)
                .where(commentLike.commentLikeId.commentId.in(commentIds))
                .transform(
                        groupBy(commentLike.commentLikeId.commentId).as(
                              list(commentLike.commentLikeId.userId)
                        )
                );
    }
}
