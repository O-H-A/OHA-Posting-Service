package com.oha.posting.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentSearchResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "부모 댓글 ID", example = "1")
    private Long parentId;

    @Schema(description = "게시물 ID", example = "1")
    private Long postId;

    @Schema(description = "댓글 내용", example = "댓글 내용")
    private String content;

    @Schema(description = "유저 ID", example = "17")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "다람쥐")
    private String userNickname;

    @Schema(description = "사용자 프로필", example = "http://112.213.123.123/files/user/1231451515.jpg")
    private String profileUrl;

    @Schema(description = "태그 유저 ID", example = "17")
    private Long taggedUserId;

    @Schema(description = "태그 유저 닉네임", example = "닉네임")
    private String taggedUserNickname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private Timestamp regDtm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private Timestamp updDtm;

    @Schema(description = "대댓글 개수", example = "10")
    private Long replyCount;

    public CommentSearchResponse(Long commentId, Long parentId, Long postId, String content, Long userId, Long taggedUserId, Timestamp regDtm, Timestamp updDtm, Long replyCount) {
        this.commentId = commentId;
        this.parentId = parentId;
        this.postId = postId;
        this.content = content;
        this.userId = userId;
        this.taggedUserId = taggedUserId;
        this.regDtm = regDtm;
        this.updDtm = updDtm;
        this.replyCount = replyCount;
    }
}
