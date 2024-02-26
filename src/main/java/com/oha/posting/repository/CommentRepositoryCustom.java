package com.oha.posting.repository;

import com.oha.posting.dto.comment.CommentSearchResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

import java.util.List;

public interface CommentRepositoryCustom {

    List<CommentSearchResponse> searchCommentList(BooleanBuilder builder, List<OrderSpecifier<?>> orderSpecifiers, int offset, int size);
}
