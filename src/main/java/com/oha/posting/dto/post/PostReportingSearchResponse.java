package com.oha.posting.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.oha.posting.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReportingSearchResponse {

    private Long reportId;

    private Long postId;

    private Long reportingUserId;

    private String reportingUserName;

    private Long reportedUserId;

    private String reportedUserName;

    private String reasonCode;

    private String reason;

    private Boolean isDone;

    private List<Action> actions;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30 15:13")
    private Timestamp actionDtm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
    @Schema(example = "2024-01-30 15:13")
    private Timestamp regDtm;

    private String thumbnailUrl;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {
        private String actionCode;
        private String action;
    }

    public static PostReportingSearchResponse toDto(Report report) {
        PostReportingSearchResponse response = new PostReportingSearchResponse();
        response.setReportId(report.getReportId());
        response.setPostId(report.getPost().getPostId());
        response.setReportingUserId(report.getUserId());
        response.setReportedUserId(report.getPost().getUserId());
        response.setReasonCode(report.getReason().getCode());
        response.setReason(report.getReason().getCodeName());
        response.setIsDone(report.getIsDone());
        response.setActions((report.getActions()).stream().map(item ->
                new Action(item.getAction().getCode(), item.getAction().getCodeName())).toList());
        response.setActionDtm(report.getActionDtm());
        response.setRegDtm(report.getRegDtm());
        return response;
    }

}