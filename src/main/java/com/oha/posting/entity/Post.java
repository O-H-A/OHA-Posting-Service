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

    @OneToOne
    @JoinColumn(name = "category_code")
    private CommonCode category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Keyword> keywords = new ArrayList<>();

    private String content;

    private Long hcode;

    private String locationDetail;

    private Timestamp regDtm;

    private Timestamp updDtm;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostFile> files;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Report> reports;

    public static Post toEntity(PostInsertRequest dto) {
        Post post = new Post();
        post.setHcode(dto.getHcode());
        post.setContent(dto.getContent());
        post.setRegDtm(new Timestamp(System.currentTimeMillis()));
        post.setLocationDetail(dto.getLocationDetail());
        return post;
    }
}
