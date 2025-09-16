package com.digitalwallet.service;

import com.digitalwallet.dto.BalanceUpdateMessage;
import com.digitalwallet.dto.NotificationMessage;

public interface NotificationService {
    void sendBalanceUpdate(Long userId, BalanceUpdateMessage balanceUpdateMessage);
    void sendTransactionNotification(Long userId, NotificationMessage notification);
    void sendGlobalNotification(NotificationMessage notification);
}
