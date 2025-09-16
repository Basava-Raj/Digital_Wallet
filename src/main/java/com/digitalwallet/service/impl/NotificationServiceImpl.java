package com.digitalwallet.service.impl;

import com.digitalwallet.dto.BalanceUpdateMessage;
import com.digitalwallet.dto.NotificationMessage;
import com.digitalwallet.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Override
    public void sendBalanceUpdate(Long userId, BalanceUpdateMessage balanceUpdate) {
//        Send to specific user's balance update channel
        messagingTemplate.convertAndSend("/topic/balance" + userId, balanceUpdate);

//        Also send as a general notification
        NotificationMessage notification = new NotificationMessage(
                "BALANCE_UPDATE",
                "Balance Updated",
                "Your wallet balance has been updated to " + balanceUpdate.getNewBalance(),
                balanceUpdate
        );
        sendTransactionNotification(userId, notification);
    }

    @Override
    public void sendTransactionNotification(Long userId, NotificationMessage notification) {
//        Send to specific user's notification channel
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }

    @Override
    public void sendGlobalNotification(NotificationMessage notification) {
//        Send to all connected users
        messagingTemplate.convertAndSend("/topic/global", notification);
    }
}
