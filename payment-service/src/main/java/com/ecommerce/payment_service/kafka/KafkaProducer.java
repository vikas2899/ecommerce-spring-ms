package com.ecommerce.payment_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendEvent(String topic, String key, Object payload) throws JsonProcessingException {
        byte[] eventBytes =  objectMapper.writeValueAsBytes(payload);
        try {
            kafkaTemplate.send(topic, key, eventBytes);
        } catch (Exception e) {
            log.error("Error sending event to topic {} with key {}", topic, key, e);
        }
    }
}
