package com.oha.posting.service;

import com.oha.posting.dto.kafka.PostCommentEvent;
import com.oha.posting.dto.kafka.PostLikeEvent;
import com.oha.posting.dto.kafka.WeatherRegEvent;
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
    private final KafkaTemplate<String, WeatherRegEvent> weatherRegTemplate;
    private final KafkaTemplate<String, PostCommentEvent> postCommentTemplate;

    public void sendPostLikeEvent(PostLikeEvent event) {
        this.postLikeTemplate.send("post-like-"+profile, event);
        log.info("post-like send: "+ event);
    }

    public void sendWeatherRegEvent(WeatherRegEvent event) {
        this.weatherRegTemplate.send("weather-reg-"+profile, event);
        log.info("weather-reg send: "+ event);
    }

    public void sendPostCommentEvent(PostCommentEvent event) {
        this.postCommentTemplate.send("post-comment-"+profile, event);
        log.info("post-comment send: "+ event);
    }
}
