package firebasecloud.com.firebasecloud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import firebasecloud.com.firebasecloud.ActiveTaskFragements.Complete;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.Pending;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Active;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Profile;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Tasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private RequestQueue queue;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar tabs_scrolling = findViewById(R.id.toolbar);
        setSupportActionBar(tabs_scrolling);



        queue = vollySingleton.getInstance().getRequestQueue();


        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.bn_task: {
                        selectedFragment = Tasks.newInstance("", "");
                        break;
                    }
                    case R.id.bn_active: {
                        selectedFragment = Active.newInstance("", "");
                        break;
                    }
                    case R.id.bn_profile: {
                        selectedFragment = Profile.newInstance("", "");
                        break;
                    }

                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame, selectedFragment);
                transaction.commit();
                return true;


            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, Tasks.newInstance("",""));
        transaction.commit();



/*        binding.btSendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int PERMISSION_ALL = 1;
                String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.VIBRATE};

           if(!hasPermissions(MainActivity.this, PERMISSIONS)){

               ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);



           }


       else
       {
           String token = Paper.book().read("token");
           sendTokenTOServer(token);


       //    requestPermission();

          // binding.btSendNotification.setEnabled(false);

       }
            }
        });*/
    }


    @SuppressLint("MissingPermission")
    private String getDeviceId() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);


        return telephonyManager.getDeviceId();


    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.VIBRATE}, 100);

    }

    private String getDeviceName() {
        String deviceName = Build.MODEL;
        String deviceMan = Build.MANUFACTURER;
        return deviceMan + " " + deviceName;
    }

    public void sendTokenTOServer(final String token) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String url = "https://stark-stream-62644.herokuapp.com/devices";
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("deviceName", getDeviceName());
                params.put("deviceId", getDeviceId());
                params.put("registrationId", token);


                final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = "Unknown error";
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = "Request timeout";
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = "Failed to connect server";
                            }

                        } else {
                            String result = new String(networkResponse.data);
                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");

                                Log.e("Error Status", status);
                                Log.e("Error Message", message);

                                if (networkResponse.statusCode == 404) {
                                    errorMessage = "Resource not found";
                                } else if (networkResponse.statusCode == 401) {
                                    errorMessage = message + " Please login again";
                                } else if (networkResponse.statusCode == 400) {
                                    errorMessage = message + " Check your inputs";
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = message + " Something is getting wrong";
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        error.printStackTrace();
                    }
                });

                queue.add(request_json);


            }
        }).start();


    }




}
