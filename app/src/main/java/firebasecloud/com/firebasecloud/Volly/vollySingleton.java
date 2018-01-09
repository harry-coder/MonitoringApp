package firebasecloud.com.firebasecloud.Volly;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import firebasecloud.com.firebasecloud.MyApplication;

/**
 * Created by Harpreet on 21/02/2017.
 */

public class vollySingleton {


    static vollySingleton instance = null;
    RequestQueue requestQueue = null;


    private vollySingleton() {
        requestQueue = Volley.newRequestQueue(MyApplication.getContext());


    }



    public static vollySingleton getInstance() {
        if (instance == null) {
            return instance = new vollySingleton();
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
