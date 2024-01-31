package com.oha.posting.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class ExternalUser {
        private Long userId;
        private String providerType;
        private String providerId;
        private String email;
        private String hashedRF;
        private String name;
        private String profileUrl;
        private String backgroundUrl;
        private Boolean isWithdraw;
        private Timestamp createdAt;
        private Timestamp updatedAt;
}
