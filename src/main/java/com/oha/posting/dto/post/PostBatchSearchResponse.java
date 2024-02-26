package com.oha.posting.dto.post;

import com.oha.posting.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostBatchSearchResponse {

    private Long postId;
    private Long userId;
    private String thumbnailUrl;
    private String mediaType;

    public static PostBatchSearchResponse toDto(Post post) {
        PostBatchSearchResponse response = new PostBatchSearchResponse();
        response.postId = post.getPostId();
        response.userId = post.getUserId();
        return response;
    }
}
