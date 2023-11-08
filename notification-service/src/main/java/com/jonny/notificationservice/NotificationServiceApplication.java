package com.jonny.notificationservice;

import com.jonny.notificationservice.dto.EmailPayload;
import com.jonny.notificationservice.services.MailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @Bean
    public Consumer<EmailPayload> notificationEventSupplier() {
        return payload -> {
            System.out.println("Received message: " + payload);
            new MailService().sendEmail(payload);
        };
    }
}