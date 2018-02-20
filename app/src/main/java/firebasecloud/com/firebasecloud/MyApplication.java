package firebasecloud.com.firebasecloud;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import io.paperdb.Paper;

/**
 * Created by Harpreet on 21/02/2017.
 */

public class MyApplication extends Application {

    static MyApplication application = null;







    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
        application = this;


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


    }




    public static MyApplication getInstance() {
        return application;
    }

    public static Context getContext() {
        return application.getApplicationContext();
    }


    public void setConnectivityListener(InternetConnectionCheck.ConnectivityReceiverListener listener) {
        InternetConnectionCheck.connectivityReceiverListener = listener;
    }
}
