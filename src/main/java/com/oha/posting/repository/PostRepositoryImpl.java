package com.oha.posting.repository;

import com.oha.posting.entity.Post;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.oha.posting.entity.QCommonCode.commonCode;
import static com.oha.posting.entity.QLike.like;
import static com.oha.posting.entity.QPost.post;
import static com.oha.posting.entity.QPostFile.postFile;

@RequiredArgsConstructor
@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPostList(BooleanBuilder builder, List<OrderSpecifier<?>> orderSpecifiers, int offset, int size) {
        return queryFactory
                .selectFrom(post)
                .join(post.category, commonCode).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(offset)
                .limit(size)
                .fetch();
    }

    @Override
    public List<Post> searchPostBatch(BooleanBuilder builder) {
        return queryFactory
                .selectFrom(post)
                .join(post.category, commonCode).fetchJoin()
                .leftJoin(post.files, postFile).fetchJoin()
                .where(builder)
                .fetch();
    }

    @Override
    public List<Post> searchPostList(Long userId) {
        return queryFactory
                .selectFrom(post)
                .join(post.category, commonCode).fetchJoin()
                .leftJoin(post.likes, like).fetchJoin()
                .where(post.isDel.eq(false).and(post.userId.eq(userId)))
                .orderBy(post.regDtm.asc())
                .fetch();
    }
}
