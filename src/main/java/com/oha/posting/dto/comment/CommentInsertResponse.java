package com.oha.posting.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

@Data
@EqualsAndHashCode(callSuper=false)
public class CommentInsertResponse extends  CommentInsertRequest {

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

    public static CommentInsertResponse toResponse(CommentInsertRequest request) {
        CommentInsertResponse response = new CommentInsertResponse();
        response.setPostId(request.getPostId());
        response.setParentId(request.getParentId());
        response.setContent(request.getContent());
        response.setTaggedUserId(request.getTaggedUserId());
        return response;
    }
}
