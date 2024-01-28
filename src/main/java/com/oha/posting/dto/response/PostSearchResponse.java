package com.oha.posting.dto.response;

import com.oha.posting.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
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
    private Long hcode;

    @Schema(example = "37.58398889")
    private Double latitude;

    @Schema(example = "127.0026222")
    private Double longitude;

    @Schema(example = "서울 성북구 하월곡동")
    private String location;

    @Schema(example = "영종도 다리")
    private String locationDetail;

    @Schema(example = "2024-01-18T10:28:47.245Z")
    private Timestamp regDtm;

    @Schema(example = "2024-01-18T10:28:47.245Z")
    private Timestamp updDtm;

    @Schema(example = "[http://localhost/images/post/adjawdalkjdasd.jpg]")
    private List<String> urls;

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
        response.regDtm = post.getRegDtm();
        response.updDtm = post.getUpdDtm();
        response.hcode = post.getHcode();
        return response;
    }
}
