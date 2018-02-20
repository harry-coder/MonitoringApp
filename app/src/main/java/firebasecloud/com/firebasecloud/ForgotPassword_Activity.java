package firebasecloud.com.firebasecloud;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.CustomElements.VollyErrors;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;


public class ForgotPassword_Activity extends AppCompatActivity {

    private EditText mobilenumber, confirmpassword, newpassword;
    private Button btnchangepassword;
    private String mobile, newpass, confirmpass;

    private RequestQueue queue;

    ProgressDialog progressDialog;

    private TextView signin;
    private AlertDialog otpDialog;

    private int count = 60;
    String password;
    private boolean isIntruppted = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password_);


        queue = vollySingleton.getInstance().getRequestQueue();

        progressDialog = new ProgressDialog(this);

        mobilenumber = findViewById(R.id.mobile);
        newpassword = findViewById(R.id.new_password);
        confirmpassword = findViewById(R.id.confirm_password);

        btnchangepassword = findViewById(R.id.btnchangepassword);

        mobilenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() != 0) {
                    char c = s.charAt(0);

                    if (c == '8' || c == '7' || c == '9') {
                        if (s.length() == 10) {

                            newpassword.requestFocus();

                        }

                    } else {
                        mobilenumber.setError("No. should start with 7,8 or 9");

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        signin = findViewById(R.id.tv_signin);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //   getFragmentManager().beginTransaction().replace(vv.getId(), new signup()).commit();

                startActivity(new Intent(ForgotPassword_Activity.this, Signup_Activity.class));
                overridePendingTransition(R.anim.activity_in, R.anim.avtivity_out);


            }
        });


        btnchangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (performValidation()) {

                    password= newpassword.getText().toString();
                    performRequest(mobilenumber.getText().toString(), ForgotPassword_Activity.this);

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_in, R.anim.back_out);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }
        if(id==android.R.id.home)
        {
            Intent i= new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            overridePendingTransition(R.anim.back_in, R.anim.back_out);

            finish();
            return true;
        }
*/

        return super.onOptionsItemSelected(item);
    }


    public void performRequest(final String mobile, final Context context) {

        final String forgotPasswordUrl = "http://www.admin-panel.adecity.com/api/forgot-password";
        progressDialog.setMessage("Waiting for server response ");
        progressDialog.show();
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("mobile", mobile);
                //  params.put("password", pass);


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, forgotPasswordUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {

                                        progressDialog.dismiss();
                                        new SweetAlertDialog(ForgotPassword_Activity.this, SweetAlertDialog.WARNING_TYPE)
                                                .setTitleText("OTP")
                                                .setContentText("We have sent you an otp")

                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sDialog) {

                                                        showOtpDialog(ForgotPassword_Activity.this);

                                                        sDialog.dismissWithAnimation();
                                                    }
                                                })
                                                .show();

                                    } else {
                                        if (response.has("msg")) {
                                            String message = response.getString("msg");

                                            progressDialog.dismiss();
                                            Alert.showAlertDialog(message, context);
                                            // btnchangepassword.setProgress(100);
                                            btnchangepassword.setText(R.string.tryagain);
                                            btnchangepassword.setBackgroundColor(Color.RED);
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    progressDialog.dismiss();
                                }

                            }
                        },


                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = VollyErrors.getInstance().showVollyError(error);

                                progressDialog.dismiss();

                                Alert.showAlertDialog(message, context);
                                error.printStackTrace();
                            }
                        });

// add the request object to the queue to be executed
                //ApplicationController.getInstance().addToRequestQueue(request_json);
                queue.add(request_json);


            }
        });
    }


    private void sendOtpWithMobile(final String number, final String input, final Context context) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                final String forgotPasswordVerifyUrl = "http://www.admin-panel.adecity.com/api/forgot-password-verify";

// Post params to be sent to the server
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("mobile", number);
                params.put("otp", input);
                params.put("password", password);

                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, forgotPasswordVerifyUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {
                                        otpDialog.dismiss();
                                        new SweetAlertDialog(ForgotPassword_Activity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Congrats")
                                                .setContentText("Your password has been changed. Login with new password")

                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sDialog) {


                                                        startActivity(new Intent(ForgotPassword_Activity.this, LoginActivity.class));
                                                        overridePendingTransition(R.anim.activity_in, R.anim.avtivity_out);

                                                        sDialog.dismissWithAnimation();
                                                    }
                                                })
                                                .show();

                                    } else {
                                        if (response.has("msg")) {
                                            String message = response.getString("msg");

                                            new SweetAlertDialog(ForgotPassword_Activity.this, SweetAlertDialog.WARNING_TYPE)
                                                    .setTitleText("Error")
                                                    .setContentText(message)

                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sDialog) {


                                                            //   getFragmentManager().beginTransaction().replace(frameView.getId(), new login()).commit();
                                                            btnchangepassword.setText(getString(R.string.tryagain));
                                                            btnchangepassword.setBackgroundColor(Color.RED);

                                                            sDialog.dismissWithAnimation();


                                                        }
                                                    })
                                                    .show();


                                            btnchangepassword.setText("Try Again?");
                                            btnchangepassword.setBackgroundColor(Color.RED);
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = VollyErrors.getInstance().showVollyError(error);

                        progressDialog.dismiss();

                        Alert.showAlertDialog(message, context);
                        error.printStackTrace();
                    }
                });

// add the request object to the queue to be executed
                //ApplicationController.getInstance().addToRequestQueue(request_json);
                queue.add(request_json);


            }
        });


    }


    public boolean performValidation() {


        mobile = mobilenumber.getText().toString();
        newpass = newpassword.getText().toString();
        confirmpass = confirmpassword.getText().toString();

        if (TextUtils.isEmpty(mobile)) {

            mobilenumber.setError(getString(R.string.field_empty));

            return false;

        }

        if (TextUtils.isEmpty(newpass)) {

            newpassword.setError("Enter new password");
            return false;
        }

        if (TextUtils.isEmpty(confirmpass)) {

            newpassword.setError(getString(R.string.field_empty));
            return false;
        }

        if (!newpass.equals(confirmpass)) {
            confirmpassword.setError(getString(R.string.password_didntmatch));

            return false;
        }

        return true;
    }


    public void showOtpDialog(final Context context) {
        final AlertDialog.Builder malert = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view1 = inflater.inflate(R.layout.otp_dialog, null);
        TextView tv_cancel = view1.findViewById(R.id.tv_cancel);
        TextView tv_submit = view1.findViewById(R.id.tv_submit);
        final TextView tv_resendOtp = view1.findViewById(R.id.tv_resend_otp);
        final TextView tv_timer = view1.findViewById(R.id.tv_timer);
        final EditText et_otp = view1.findViewById(R.id.et_otp);


        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                otpDialog.dismiss();
            }
        });

        startTimer(tv_timer, tv_resendOtp);
        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = et_otp.getText().toString();

                if (!TextUtils.isEmpty(otp)) {

                    sendOtpWithMobile(mobile, otp, context);

                    //otpDialog.dismiss();
                } else {
                    et_otp.setError("Enter otp");
                }
            }
        });

        tv_resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                startTimer(tv_timer,tv_resendOtp);
                tv_resendOtp.setVisibility(View.INVISIBLE);
                tv_timer.setVisibility(View.VISIBLE);
                resendOtp(mobile, context);
                startTimer(tv_timer, tv_resendOtp);


                //   TodayRate.showAlertDialog("Under maintenance",context);

            }
        });


        malert.setView(view1);

        otpDialog = malert.create();
        otpDialog.setCanceledOnTouchOutside(false);
        otpDialog.show();

    }

    public void startTimer(final TextView tv_timer, final TextView tv_resendOtp) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                while (!isIntruppted) {
                    try {

                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_timer.setText(String.valueOf(count));

                            }
                        });
                        count--;


                        if (count == 0) {
                            isIntruppted = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_resendOtp.setVisibility(View.VISIBLE);

                                    tv_timer.setVisibility(View.INVISIBLE);

                                }
                            });
                            count = 60;

                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
                count = 60;
                isIntruppted = false;


            }
        }).start();


    }


    public void resendOtp(final String number, final Context context) {
        final String resendOtpUrl = "http://www.admin-panel.adecity.com/api/forgot-password";

        progressDialog.setMessage("Resending Otp");
        progressDialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("mobile", number);


                final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, resendOtpUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {

                                        //   otpDialog.dismiss();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                Alert.showAlertDialog("Otp sent successfully", context);
                                                progressDialog.dismiss();

                                            }
                                        }, 2000);


                                    } else {
                                        String message = response.getString("msg");
                                        progressDialog.dismiss();


                                        Alert.showAlertDialog(response.getString("msg"), context);
                                        //  btnSignIn.setProgress(100);
                                    }

                                } catch (JSONException e) {
                                    progressDialog.dismiss();

                                    e.printStackTrace();
                                }

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
                        Alert.showAlertDialog(errorMessage, context);
                        error.printStackTrace();
                    }
                });

                queue.add(request_json);


            }
        }).start();

    }


    public void backToLogin(View view) {

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        overridePendingTransition(R.anim.back_in, R.anim.back_out);
    }
}