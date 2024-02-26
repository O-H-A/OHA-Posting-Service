package com.oha.posting.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalUser {
        private Long userId;
        private String name;
        private String profileUrl;
}
