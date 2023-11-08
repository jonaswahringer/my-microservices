package com.jonny.notificationservice.services;

import com.jonny.notificationservice.dto.EmailPayload;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    @Value("${from.email.address}")
    private String fromEmailAddress;

    private JavaMailSender mailSender;

    public MailService() {

    }

    public MailService(JavaMailSender senderBean) {
        this.mailSender = senderBean;
    }

    public void sendEmail(EmailPayload payload) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(fromEmailAddress);
            helper.setTo(payload.getRecipient());
            helper.setSubject(payload.getSubject());
            helper.setText(payload.getContent(), true);
            mailSender.send(message);
        } catch (MessagingException messagingException) {
            messagingException.printStackTrace();
        }
    }

}
