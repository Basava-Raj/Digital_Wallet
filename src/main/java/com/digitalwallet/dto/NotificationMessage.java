package com.digitalwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@Data
public class NotificationMessage {
    private String type;
    private String title;
    private String message;
    private Object data;
    private LocalDateTime timestamp;

    public NotificationMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationMessage(String type, String title, String message, Object data) {
        this();
        this.type = type;
        this.title = title;
        this.message = message;
        this.data = data;
    }
}
