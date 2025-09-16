package com.digitalwallet.controller;

import com.digitalwallet.dto.NotificationMessage;
import com.digitalwallet.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private NotificationService notificationService;

    @MessageMapping("/connect")
    @SendTo("/topic/global")
    public NotificationMessage handleConnection(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
//        Store user session
        headerAccessor.getSessionAttributes().put("userId", userId);

        return new NotificationMessage(
                "USER_CONNECTED",
                "User Connected",
                "User " + userId + " is now online",
                userId
        );
    }

    @MessageMapping("/disconnect")
    @SendTo("/topic/global")
    public  NotificationMessage handleDisconnection(@Payload String userId) {
        return new NotificationMessage(
                "USER_DISCONNECTED",
                "User Disconnected",
                "User " + userId + " went offline",
                userId
        );
    }

    public void handlePing(@Payload String userId) {
//        Send pong back to specific user
        NotificationMessage pong = new NotificationMessage(
                "PONG",
                "Connecion Active",
                "WebSocket connection is active",
                System.currentTimeMillis()
        );
        notificationService.sendTransactionNotification(Long.parseLong(userId), pong);
    }

}
