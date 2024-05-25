package com.oha.posting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

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

    @ManyToOne
    private CommonCode reason;

    private Boolean isDone;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReportAction> actions;

    private Timestamp actionDtm;

    private Timestamp regDtm;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    public Report (Long userId, CommonCode reason, Post post) {
        this.userId = userId;
        this.reason = reason;
        this.post = post;
        this.isDone = false;
        this.regDtm = new Timestamp(System.currentTimeMillis());
    }
}
