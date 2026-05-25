package com.bank.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaCreditCardProducerConfig {

    @Bean
    public NewTopic applyCreditCardTopic() {
        return TopicBuilder.name(KafkaConstants.CREDIT_CARD_APPLICATION_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic creditCardStatusTopic() {
        return TopicBuilder.name(KafkaConstants.CREDIT_CARD_STATUS_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionPaymentTopic() {
        return TopicBuilder.name(KafkaConstants.TRANSACTION_PAYMENT_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }



    @Bean
    public NewTopic creditCardTransactionTopic() {
        return TopicBuilder.name(KafkaConstants.CREDIT_CARD_TRANSACTION_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }



    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
