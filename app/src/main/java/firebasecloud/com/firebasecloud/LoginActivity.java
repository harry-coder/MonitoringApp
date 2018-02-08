package firebasecloud.com.firebasecloud;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnBackPressListener;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.databinding.ActivityLoginBinding;
import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding dataBinding;
    private ProgressDialog progressDialog;
    private RequestQueue queue;

    public static int loginButtonWidth;
    private String mobileNumber, userpassword;
    private Handler handler;
    public static String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        fetchMobNumber();
        progressDialog = new ProgressDialog(this);

        queue = vollySingleton.getInstance().getRequestQueue();
        handler = new Handler();




        dataBinding.flLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInternetConnection()) {

                 /*   progressDialog.setMessage("Logging in..");
                    progressDialog.show();
*/
                    mobileNumber = dataBinding.mobile.getText().toString();
                    userpassword = dataBinding.password.getText().toString();


                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {

                            Paper.book().write("contact", mobileNumber);

                        }
                    });

                    loginWithCredentials(mobileNumber, userpassword);

                }
                else {
                    netConnectivityDialog(LoginActivity.this);
                }

            }
        });

        dataBinding.tvSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,Signup_Activity.class));
                overridePendingTransition(R.anim.activity_in, R.anim.avtivity_out);

            }
        });
    }

    public void fetchMobNumber()
    {
        if(Paper.book().exist("contact"))
        {
            new Handler().post(new Runnable() {
                @Override
                public void run() {

                    dataBinding.mobile.setText(Paper.book().read("contact").toString());
                }
            });


        }


    }
    public void loginWithCredentials(final String mobile, final String password) {

        final String loginUrl = "http://www.admin-panel.adecity.com/api/login";
            animateButtonWidth();
            fadeOutTextAndShowProgressDialog();



            new Thread(new Runnable() {
                @Override
                public void run() {
                    final HashMap<String, String> params = new HashMap<String, String>();

                    params.put("password", password);
                    params.put("mobile", mobile);


                    JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, loginUrl, new JSONObject(params),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {


                                    System.out.println(response);
                                    try {
                                        boolean success = response.getBoolean("success");


                                        if (success) {


                                            //deleting the user logout out status because he is loging in now
                                            Paper.book().delete("isUserLoggedOut");
                                            userId = response.getString("userid");

                                            if(userId!=null)
                                            {
                                                Paper.book().write("userId",userId);
                                            }

                                            nextAction();

                                            //            Paper.book().write("userId", userId);

                                        } else {

                                            Alert.showAlertDialog(response.getString("msg"),LoginActivity.this);
                                            deAnimateButtonWidth();
                                            fadeOutProgressbarAndShowText();
                                  //          progressDialog.dismiss();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        System.out.println(e);
                                    //    progressDialog.dismiss();
                                    }

                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            deAnimateButtonWidth();
                            fadeOutProgressbarAndShowText();
                            NetworkResponse networkResponse = error.networkResponse;
                            String errorMessage = "Unknown error";
                            if (networkResponse == null) {
                                if (error.getClass().equals(TimeoutError.class)) {
                                    errorMessage = "Request timeout";
                                } else if (error.getClass().equals(NoConnectionError.class)) {
                                    errorMessage = "Failed to connect server";
                                }
                                progressDialog.dismiss();
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
                                progressDialog.dismiss();
                            }
                            Alert.showAlertDialog(errorMessage, LoginActivity.this);
                            error.printStackTrace();
                        }

                    });


                    queue.add(request_json);
                }
            }).start();



    }
    public static void netConnectivityDialog(Context context) {

        //TodayRate.showAlertDialog("No internet available ",);

        DialogPlus dialog = DialogPlus.newDialog(context)
                .setContentHolder(new ViewHolder(R.layout.no_internet_dialog)).setGravity(Gravity.TOP)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {


                    }


                }).setCancelable(false).setOnBackPressListener(new OnBackPressListener() {
                    @Override
                    public void onBackPressed(DialogPlus dialogPlus) {


                        dialogPlus.dismiss();


                    }
                }).setCancelable(true).setGravity(Gravity.CENTER).setContentBackgroundResource(Color.TRANSPARENT)
                // This will enable the expand feature, (similar to android L share dialog)
                .create();
        dialog.show();


    }



    //check internet connection
    public boolean checkInternetConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting() && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {

            return true;

        } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            return false;
        } else return false;
    }




    public void showProgressDialog() {
        dataBinding.pbProgres.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_IN);
        dataBinding.pbProgres.setVisibility(View.VISIBLE);
    }

    public int getFabWidth() {
        return (int) getResources().getDimension(R.dimen.fab_width);
    }

    public void fadeOutTextAndShowProgressDialog() {
        dataBinding.tvLogin.animate().alpha(0f).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showProgressDialog();
            }
        }).start();

    }

    public void fadeOutProgressbarAndShowText() {
        dataBinding.tvLogin.animate().alpha(1f).setDuration(250).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //     showProgressDialog();

                //  dataBinding.tvLogin.animate().alpha(1f).setDuration(250);
                dataBinding.tvLogin.setText("Snap!! Try Again?");
                dataBinding.flLogin.setBackgroundColor(Color.RED);

                dataBinding.pbProgres.setVisibility(View.GONE);

            }
        }).start();
    }

    public void deAnimateButtonWidth() {
        dataBinding.flLogin.setEnabled(true);
        /// Toast.makeText(this, "Inside demate", Toast.LENGTH_SHORT).show();
        ValueAnimator anim = ValueAnimator.ofInt(getFabWidth(), loginButtonWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                int val = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = dataBinding.flLogin.getLayoutParams();
                layoutParams.width = val;
                //  System.out.println("width is "+layoutParams.width);
                dataBinding.flLogin.requestLayout();
/*
                dataBinding.pbProgres.setVisibility(View.INVISIBLE);
                dataBinding.tvLogin.animate().alpha(1f).setDuration(250);
                dataBinding.tvLogin.setText("Snap!! Try Again?");
                dataBinding.flLogin.setBackgroundColor(Color.RED);
*/


            }
        });
        anim.setDuration(250);
        anim.start();
    }

    public void animateButtonWidth() {

        dataBinding.flLogin.setEnabled(false);
        loginButtonWidth = dataBinding.flLogin.getMeasuredWidth();
        ValueAnimator anim = ValueAnimator.ofInt(loginButtonWidth, getFabWidth());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
               // System.out.println("Inside listener");

                int val = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = dataBinding.flLogin.getLayoutParams();
                layoutParams.width = val;
                dataBinding.flLogin.requestLayout();

            }
        });
        anim.setDuration(250);
        anim.start();
    }

    public void nextAction() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    revealButton();
                }
                fadOutProgressDialog();
                startNextActivity();
                //         dataBinding.elasticDownloadView.success();

            }
        }, 2000);
    }

    private void startNextActivity() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.activity_in, R.anim.avtivity_out);
                //    Toast.makeText(LoginActivity.this, "Next Activity time", Toast.LENGTH_SHORT).show();

            }
        }, 200);
    }

    private void fadOutProgressDialog() {
        dataBinding.pbProgres.animate().alpha(0f).setDuration(250).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void revealButton() {

        dataBinding.flLogin.setElevation(0f);
        dataBinding.reveal.setVisibility(View.VISIBLE);
        int cx = dataBinding.reveal.getWidth();
        int cy = dataBinding.reveal.getHeight();

        int x = (int) (getFabWidth() / 2 + dataBinding.flLogin.getX());
        int y = (int) (getFabWidth() / 2 + dataBinding.flLogin.getY());

        float finalRadius = Math.max(cx, cy) * 1.2f;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animator reveal = ViewAnimationUtils.createCircularReveal(dataBinding.reveal, x, y, getFabWidth(), finalRadius);
            reveal.setDuration(250);
            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    finish();
                }
            });
            reveal.start();
        }


    }

    public boolean runTimePermission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);

            return true;
        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                runTimePermission();
            }

        }

    }


    public void forgotPassword(View view) {
        startActivity(new Intent(LoginActivity.this,ForgotPassword_Activity.class));
        overridePendingTransition(R.anim.activity_in, R.anim.avtivity_out);


    }
}
