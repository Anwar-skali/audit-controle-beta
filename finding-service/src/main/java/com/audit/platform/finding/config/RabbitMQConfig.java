package com.audit.platform.finding.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${audit.rabbitmq.exchange:audit.events.exchange}")
    private String exchange;

    @Value("${audit.rabbitmq.queue:audit.findings.queue}")
    private String queue;

    @Value("${audit.rabbitmq.routingkey:audit.finding.created}")
    private String routingKey;

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue).to(auditExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

