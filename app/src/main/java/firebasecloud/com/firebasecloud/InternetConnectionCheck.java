package firebasecloud.com.firebasecloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Harpreet on 15/02/2018.
 */

public class InternetConnectionCheck extends BroadcastReceiver {

    public static ConnectivityReceiverListener connectivityReceiverListener;


    public InternetConnectionCheck()
    {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isConnected=LoginActivity.checkInternetConnection(context);

        if(connectivityReceiverListener!=null)
        {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }

    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}
