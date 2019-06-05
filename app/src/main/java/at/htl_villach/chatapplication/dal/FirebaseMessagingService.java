package at.htl_villach.chatapplication.dal;

import android.app.Notification;
import android.app.NotificationManager;

import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;

import at.htl_villach.chatapplication.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();

        String notification_type = remoteMessage.getData().get("type");
        String notification_sender = remoteMessage.getData().get("sender");


        Notification mBuilder = new Notification.Builder(this)
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                //.setLargeIcon(aBitmap)
                .build();

        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder);

    }

}
