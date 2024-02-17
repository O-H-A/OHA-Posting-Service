package com.oha.posting.repository;

import com.oha.posting.entity.Post;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

import java.util.List;

public interface PostRepositoryCustom {

    List<Post> searchPostList(BooleanBuilder builder, List<OrderSpecifier<?>> orderSpecifiers, int offset, int size);

    List<Post> searchPostBatch(BooleanBuilder builder);
}
