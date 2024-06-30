package com.oha.posting.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.oha.posting.dto.external.ExternalLocation;
import com.oha.posting.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchResponse {

    private Long postId;

    private Long userId;

    private String userName;

    private Boolean isLike;

    private Integer likeCount;

    private String categoryCode;

    private String categoryName;

    private List<String> keywords;

    @Schema(example = "별밤투어")
    private String content;

    @Schema(example = "4215032000")
    private Long regionCode;

    @Schema(example = "강원도")
    private String firstAddress;

    @Schema(example = "강릉시")
    private String secondAddress;

    @Schema(example = "왕산면")
    private String thirdAddress;

    @Schema(example = "안반데기")
    private String locationDetail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private LocalDateTime regDtm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Schema(example = "2024-01-30T15:13:37.875")
    private LocalDateTime updDtm;

    @Schema(description = "파일URL")
    private List<PostSearchFile> files = new ArrayList<>();

    @Schema(description = "썸네일URL", example = "http://localhost/images/post/adjawdalkjdasd.jpg")
    private String thumbnailUrl;

    @Schema(description = "프로필 이미지 URL")
    private String profileUrl;

    @Schema(description = "본인 게시물 여부")
    private Boolean isOwn;

    public static PostSearchResponse toDto(Post post, Long userId) {
        PostSearchResponse response = new PostSearchResponse();
        response.postId = post.getPostId();
        response.userId = post.getUserId();
        response.isLike = (post.getLikes()).stream().anyMatch(like -> userId.equals(like.getLikeId().getUserId()));
        response.likeCount = post.getLikes().size();
        response.categoryCode = post.getCategory().getCode();
        response.categoryName = post.getCategory().getCodeName();
        response.keywords = (post.getKeywords()).stream().map(Keyword::getKeywordName).toList();
        response.content = post.getContent();
        response.locationDetail = post.getLocationDetail();
        response.regDtm = post.getRegDtm().toLocalDateTime();
        response.updDtm = post.getUpdDtm() == null ? null : post.getUpdDtm().toLocalDateTime();
        response.regionCode = post.getRegionCode();
        response.isOwn = post.getUserId().equals(userId);
        return response;
    }

    @Schema(hidden = true)
    public void setLocationInfo(ExternalLocation location) {
        this.firstAddress = location.getFirstAddress();
        this.secondAddress = location.getSecondAddress();
        this.thirdAddress = location.getThirdAddress();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostSearchFile {
        @Schema(description = "파일URL", example = "http://localhost/images/post/adjawdalkjdasd.jpg")
        private String url;

        @Schema(description = "파일순서", example = "0")
        private Integer seq;
    }
}
