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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private Timestamp regDtm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Seoul")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private Timestamp updDtm;

    public static CommentUpdateResponse toResponse(Comment comment) {
        CommentUpdateResponse response = new CommentUpdateResponse();
        response.setCommentId(comment.getCommentId());
        response.setContent(comment.getContent());
        response.setRegDtm(comment.getRegDtm());
        response.setUpdDtm(comment.getUpdDtm());
        return response;
    }
}
