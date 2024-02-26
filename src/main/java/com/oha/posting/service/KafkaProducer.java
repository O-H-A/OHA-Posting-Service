package com.oha.posting.service;

import com.oha.posting.dto.kafka.PostLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    @Value("${spring.profiles.active}")
    private String profile;
    private final KafkaTemplate<String, PostLikeEvent> postLikeTemplate;

    public void sendPostLikeEvent(PostLikeEvent event) {
        this.postLikeTemplate.send("post-like-"+profile, event);
        log.info("post-like send: "+ event);
    }

}
