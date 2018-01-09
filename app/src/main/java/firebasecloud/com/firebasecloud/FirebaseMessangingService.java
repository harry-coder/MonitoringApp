package firebasecloud.com.firebasecloud;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import firebasecloud.com.firebasecloud.CallClasses.AcceptedOffer;

/**
 * Created by Harpreet on 15/11/2017.
 */

public class FirebaseMessangingService extends FirebaseMessagingService {
    String message;
    int NOTIFICATION_ID = 100;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        System.out.println(remoteMessage.getData().toString());


        Map<String, String> params = remoteMessage.getData();
        JSONObject object = new JSONObject(params);

        try {

            message = object.getString("message");

            System.out.println("This is message " + message);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(this, AcceptedOffer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);


    }
}
