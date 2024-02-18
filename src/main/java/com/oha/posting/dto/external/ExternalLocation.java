package com.oha.posting.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalLocation implements Serializable {
    private String code;
    private String firstAddress;
    private String secondAddress;
    private String thirdAddress;
    private Boolean isHcode;
    private Boolean isBcode;
}
