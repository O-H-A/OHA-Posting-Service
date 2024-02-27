package com.oha.posting.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.oha.posting.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode(callSuper=false)
public class CommentUpdateResponse extends  CommentUpdateRequest {

    private Long postId;
    private Long parentId;
    private Long commentId;
    private Long userId;
    private String userNickname;
    private Long taggedUserId;
    private String taggedUserNickname;
    private String profileUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private Timestamp regDtm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private Timestamp updDtm;

    public static CommentUpdateResponse toResponse(Comment comment) {
        CommentUpdateResponse response = new CommentUpdateResponse();
        response.setPostId(comment.getPost().getPostId());
        response.setParentId(comment.getParent() != null ? comment.getParent().getCommentId() : null);
        response.setCommentId(comment.getCommentId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUserId());
        response.setRegDtm(comment.getRegDtm());
        response.setUpdDtm(comment.getUpdDtm());
        return response;
    }
}
