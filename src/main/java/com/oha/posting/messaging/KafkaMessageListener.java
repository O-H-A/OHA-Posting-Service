package com.oha.posting.messaging;

import com.oha.posting.dto.kafka.UserWithdrawEvent;
import com.oha.posting.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final PostService postService;

    @KafkaListener(
            topics = "user-withdraw-${PROFILE}",
            groupId = "posting-group"
    )
    public void handleUserWithdraw(ConsumerRecord<String, UserWithdrawEvent> record) {
        UserWithdrawEvent user = record.value();
        log.info("{}: {}", record.topic(), user.toString());

        try {
            // 게시물 삭제
            postService.deletePostByUserId(user.getUserId());

        } catch (Exception e) {
            log.warn("Post delete failed", e);
        }
    }

}
