package com.jonny.notificationservice.dto;

import lombok.Data;

@Data
public class EmailPayload {
    String recipient;
    String subject;
    String content;
}