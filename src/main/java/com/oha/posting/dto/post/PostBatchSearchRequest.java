package com.oha.posting.dto.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostBatchSearchRequest {
    @NotNull
    @Size(min = 1, max = 100)
    private List<Long> postIds;
}
