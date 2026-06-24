package com.credit.loan.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String LOAN_SUBMITTED_TOPIC = "loan-applications";
    public static final String CREDIT_EVALUATED_TOPIC = "credit-evaluations";

    @Bean
    public NewTopic loanSubmittedTopic() {
        return TopicBuilder.name(LOAN_SUBMITTED_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic creditEvaluatedTopic() {
        return TopicBuilder.name(CREDIT_EVALUATED_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();
    }
}
