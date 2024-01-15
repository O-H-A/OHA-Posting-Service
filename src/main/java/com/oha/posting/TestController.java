package com.oha.posting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name="테스트", description = "Posting 테스트용 API")
@RestController
public class TestController {

    @Operation(summary = "문자열 입력", description = "입력한 문자열이 반환됩니다.")
    @Parameter(name = "str", description = "문자열")
    @GetMapping("/api/posting/test")
    public String test(@RequestParam String str) {
        return str;
    }
}
