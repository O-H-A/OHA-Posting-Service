package com.oha.posting.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeEvent {
    private Long postId;
    private Long userId;
    private Long likeUserId;
    private String mediaType;
}
