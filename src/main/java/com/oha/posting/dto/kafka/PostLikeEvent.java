package com.oha.posting.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeEvent {
    private Long post_id;
    private Long user_id;
    private Long like_user_id;
    private String media_type;
    private String thumbnail_url;
}
