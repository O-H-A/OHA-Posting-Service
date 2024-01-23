package com.oha.posting.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_post_likes")
public class Like {

    @EmbeddedId
    private LikeId likeId;

    @Schema(hidden = true)
    @ManyToOne
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;
}
