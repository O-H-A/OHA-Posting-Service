package com.oha.posting.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentEvent {
    private Long post_id;
    private Long user_id;
    private Long comment_user_id;
    private String comment_content;
    private String thumbnail_url;
}
