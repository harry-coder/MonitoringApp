package firebasecloud.com.firebasecloud.CallClasses;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Harpreet on 08/12/2017.
 */

public class NotificationDismiss extends AppCompatActivity {
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(100);
        finish(); // since finish() is called in onCreate(), onDestroy() will be called immediately
    }

    public static PendingIntent getDismissIntent(int notificationId, Context context) {
        Intent intent = new Intent(context, NotificationDismiss.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(NOTIFICATION_ID, notificationId);
        PendingIntent dismissIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return dismissIntent;
    }
}
