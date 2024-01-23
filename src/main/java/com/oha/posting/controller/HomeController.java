package com.oha.posting.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="홈", description = "홈 API")
@RequestMapping("/api/home")
@RestController
public class HomeController {

}
