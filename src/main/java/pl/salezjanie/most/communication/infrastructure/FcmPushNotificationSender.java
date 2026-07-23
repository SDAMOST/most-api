package pl.salezjanie.most.communication.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.salezjanie.most.communication.domain.PushNotificationSender;

import java.util.List;

@Component
public class FcmPushNotificationSender implements PushNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(FcmPushNotificationSender.class);

    @Override
    public void sendPushNotification(List<String> deviceTokens, String title, String body) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            return;
        }
        
        // TODO: In the future, this will use FirebaseMessaging.getInstance().sendMulticast(message)
        // For now, we simulate sending the FCM notification
        
        log.info("=== SIMULATED FCM PUSH NOTIFICATION ===");
        log.info("To Tokens: {}", deviceTokens);
        log.info("Title: {}", title);
        log.info("Body: {}", body);
        log.info("=======================================");
    }
}
