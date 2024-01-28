package com.oha.posting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_post_reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private Long userId;

    private String content;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    public Report (Long userId, String content, Post post) {
        this.userId = userId;
        this.content = content;
        this.post = post;
    }
}
