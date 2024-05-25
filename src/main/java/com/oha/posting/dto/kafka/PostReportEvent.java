package com.oha.posting.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReportEvent {
    private Long post_id;
    private Long report_id;
    private Long reporting_user_id;
    private Long reported_user_id;
    private String report_reason;
    private String thumbnail_url;
}
