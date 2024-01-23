package com.oha.posting.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tb_post_files")
public class PostFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private String savePath;
    private String url;

    public PostFile (Post post, String filePath, String url) {
        this.post = post;
        this.savePath = filePath;
        this.url = url;
    }
}
