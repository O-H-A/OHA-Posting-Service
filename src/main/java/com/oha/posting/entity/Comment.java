package com.oha.posting.entity;

import com.oha.posting.dto.comment.CommentInsertRequest;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_post_comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private String content;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    private Long userId;

    private Long taggedUserId;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> child = new ArrayList<>();

    private Boolean isParent;

    private Boolean isDel;

    private Timestamp regDtm;

    private Timestamp updDtm;

    public static Comment toEntity(CommentInsertRequest dto) {
        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setRegDtm(new Timestamp(System.currentTimeMillis()));
        comment.setIsDel(false);
        return comment;
    }
}
