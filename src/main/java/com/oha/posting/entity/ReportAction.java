package com.oha.posting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_post_report_actions")
public class ReportAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionId;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne
    @JoinColumn(name = "action_code")
    private CommonCode action;

    public ReportAction (Report report, CommonCode action) {
        this.report = report;
        this.action = action;
    }
}
