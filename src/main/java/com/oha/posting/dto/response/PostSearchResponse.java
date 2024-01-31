package com.oha.posting.dto.response;

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

    private String userNickname;

    private List<Long> likeUsers;

    private Integer likeCount;

    private String categoryCode;

    private List<String> keywords;

    @Schema(example = "오하늘")
    private String content;

    @Schema(example = "1111065000")
    private Long regionCode;

    @Schema(example = "서울")
    private String firstAddress;

    @Schema(example = "성북구")
    private String secondAddress;

    @Schema(example = "하월곡동")
    private String thirdAddress;

    @Schema(example = "영종도 다리")
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

    private List<PostSearchFile> files = new ArrayList<>();

    public static PostSearchResponse toDto(Post post) {
        PostSearchResponse response = new PostSearchResponse();
        response.postId = post.getPostId();
        response.userId = post.getUserId();
        response.likeUsers = (post.getLikes()).stream().map(Like::getLikeId).map(LikeId::getUserId).toList();
        response.likeCount = response.getLikeUsers().size();
        response.categoryCode = post.getCategory().getCode();
        response.keywords = (post.getKeywords()).stream().map(Keyword::getKeywordName).toList();
        response.content = post.getContent();
        response.locationDetail = post.getLocationDetail();
        response.regDtm = post.getRegDtm().toLocalDateTime();
        response.updDtm = post.getUpdDtm() == null ? null : post.getUpdDtm().toLocalDateTime();
        response.regionCode = post.getRegionCode();
        return response;
    }

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
