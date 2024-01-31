package com.oha.posting.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalLocation {
    private String code;
    private String firstAddress;
    private String secondAddress;
    private String thirdAddress;
    private Boolean isHcode;
    private Boolean isBcode;
}
