package pl.salezjanie.most.communication.domain;

import java.util.List;

public interface PushNotificationSender {
    void sendPushNotification(List<String> deviceTokens, String title, String body);
}
