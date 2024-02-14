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
    private String directory;
    private String fileName;

    private String thumbnailName;
    private Integer seq;

    public PostFile (Post post, String directory, String fileName, String thumbnailName, Integer seq) {
        this.post = post;
        this.directory = directory;
        this.fileName = fileName;
        this.thumbnailName = thumbnailName;
        this.seq = seq;
    }
}
