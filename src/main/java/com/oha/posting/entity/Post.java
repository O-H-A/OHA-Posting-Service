package com.oha.posting.entity;

import com.oha.posting.dto.request.PostInsertRequest;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_post_posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private Long userId;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private CommonCode category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Keyword> keywords = new ArrayList<>();

    private String content;

    private Long regionCode;

    private String locationDetail;

    private Timestamp regDtm;

    private Timestamp updDtm;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PostFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports;

    private Boolean isDel;

    private Timestamp delDtm;

    public static Post toEntity(PostInsertRequest dto) {
        Post post = new Post();
        post.setRegionCode(dto.getRegionCode());
        post.setContent(dto.getContent());
        post.setRegDtm(new Timestamp(System.currentTimeMillis()));
        post.setLocationDetail(dto.getLocationDetail());
        post.setIsDel(false);
        return post;
    }
}
