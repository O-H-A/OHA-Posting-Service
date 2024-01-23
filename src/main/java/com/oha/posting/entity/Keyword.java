package com.oha.posting.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_post_keywords")
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long keywordId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String keywordName;

    public Keyword (Post post, String keywordName) {
        this.post = post;
        this.keywordName = keywordName;
    }
}
