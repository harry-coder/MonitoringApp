package firebasecloud.com.firebasecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.CustomElements.Localities;
import firebasecloud.com.firebasecloud.CustomElements.VollyErrors;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.databinding.ActivitySignupBinding;
import io.paperdb.Paper;

public class Signup_Activity extends AppCompatActivity {

    ActivitySignupBinding binding;
    ProgressDialog progressDialog;
    RequestQueue queue;
    String mobile, password, passwordConfirm, userName;
    public static String city, userType;
    ArrayAdapter<String> spinnerArrayAdapter;
    private AlertDialog otpDialog;
    int count = 60;
    boolean isIntruppted = false;
    VollyErrors vollyErrors;
    HashMap<String, String> citiesMap;
    public static String cityId;
    ArrayList<String> localityKey;
    public static String selectedCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup_);

        progressDialog = new ProgressDialog(this);

        queue = vollySingleton.getInstance().getRequestQueue();

        vollyErrors = VollyErrors.getInstance();
        citiesMap = new HashMap<>();
        localityKey = new ArrayList<>();


        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInternetConnection()) {
                    if (autheticateDetails()) {
                        try {
                            signupWithCredentials(mobile, password, userName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } else LoginActivity.netConnectivityDialog(Signup_Activity.this);

            }
        });

        binding.tvSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Signup_Activity.this, LoginActivity.class));
                overridePendingTransition(R.anim.back_in, R.anim.back_out);

            }
        });


        getUserCities();


        setItemsForUserTypeSpinner(R.array.user_type);

       /* binding.tvSelectLocality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedItem=binding.spCity.getSelectedItem().toString();
                if(!selectedItem.equalsIgnoreCase("select city")) {
                    if (binding.spCity.getSelectedItem() != null) {
                        startActivity(new Intent(Signup_Activity.this, SearchLocality.class));

                    } else {
                        Toast.makeText(Signup_Activity.this, "Please select city first", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
*/
    }

    public boolean autheticateDetails() {
        mobile = binding.mobilenumber.getText().toString();
        password = binding.password.getText().toString();
        passwordConfirm = binding.confirmPassword.getText().toString();
        userName = binding.name.getText().toString();
        userType = (String) binding.spUserType.getSelectedItem();
        city = (String) binding.spCity.getSelectedItem();


        if (TextUtils.isEmpty(mobile)) {
            binding.mobilenumber.setError(getString(R.string.provide_contact));
            binding.mobilenumber.requestFocus();
            return false;

        } else if (TextUtils.isEmpty(password)) {
            binding.password.setError(getString(R.string.field_empty));
            binding.password.requestFocus();
            return false;

        } else if (TextUtils.isEmpty(passwordConfirm)) {
            binding.confirmPassword.setError(getString(R.string.confirmpassword));
            binding.confirmPassword.requestFocus();
            return false;

        } else if (!password.equalsIgnoreCase(passwordConfirm)) {
            binding.confirmPassword.setError(getString(R.string.password_didntmatch));
            binding.confirmPassword.requestFocus();

            return false;
        } else if (TextUtils.isEmpty(userName)) {
            binding.name.setError(getString(R.string.enter_name));
            binding.name.requestFocus();

            return false;
        } else if (TextUtils.isEmpty(city)) {
            binding.tvEmptyCityWarning.setVisibility(View.VISIBLE);

            binding.tvEmptyCityWarning.setTextColor(Color.RED);
            binding.spCity.requestFocus();

            return false;
        } else if (TextUtils.isEmpty(userType)) {
            binding.tvEmptyWarning.setVisibility(View.VISIBLE);

            binding.tvEmptyWarning.setTextColor(Color.RED);
            binding.spUserType.requestFocus();

            return false;
        }
        return true;
    }


    public void signupWithCredentials(final String mobile, final String password, final String name) throws JSONException {

        final String token = Paper.book().read("token");

        final String signupUrl = "http://www.admin-panel.adecity.com/api/register";


/*
        JSONArray clusterLocalityArray=new JSONArray();

        //this could crash because lenght could be different...

        for (int i=0;i<SearchLocality.clusterLocalityNames.length;i++)
        {
            ArrayList<String> list=SearchLocality.clusterLocalityNames[i];
            JSONObject obj=new JSONObject();
            JSONArray array=new JSONArray();
            for(int j=0;j<list.size();j++)
            {
                array.put(list.get(j));
            }
            obj.put("names",array);
            obj.put("group_name",SearchLocality.globalClusterNames.get(i).getClusterName());
            clusterLocalityArray.put(obj);
        }


        final JSONArray array = new JSONArray();


      ArrayList<Localities> list=SearchLocality.selectedLocalities;




      if(!list.isEmpty()) {

          for (int i = 0; i < list.size(); i++) {
              array.put(list.get(i).getLocalityName());
          }
      }
*/


        new Handler().post(new Runnable() {
            @Override
            public void run() {

                progressDialog.setMessage("Signing in.. ");
                progressDialog.show();
                HashMap<String, Object> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("password", password);

                params.put("user_type", userType.toLowerCase());
                params.put("name", name);
                params.put("registrationId", token);

                params.put("user_city", city);
                //this need to be changed..
                // params.put("user_locality", array);


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, signupUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {
                                        progressDialog.dismiss();
                                        // btnSignIn.setProgress(100);
                                        new SweetAlertDialog(Signup_Activity.this, SweetAlertDialog.WARNING_TYPE)
                                                .setTitleText("OTP")
                                                .setContentText("We have sent you an otp")

                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sDialog) {

                                                        showOtpDialog(Signup_Activity.this, mobile);
                                                        sDialog.dismissWithAnimation();
                                                    }
                                                })
                                                .show();


                                        //  sendOtp(mobile);

                                    /*    Toast.makeText(Signup_Activity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Signup_Activity.this,LoginActivity.class));
                                        overridePendingTransition(R.anim.back_in, R.anim.back_out);*/
                                    } else {

                                        String message = response.getString("msg");

                                        progressDialog.dismiss();
                                        Alert.showAlertDialog(message, Signup_Activity.this);

                                        // btnSignIn.setProgress(100);
                                        binding.btnSignIn.setText(R.string.tryagain);
                                        binding.btnSignIn.setBackgroundColor(Color.RED);

                                    }

                                } catch (JSONException e) {
                                    Toast.makeText(Signup_Activity.this, "" + e, Toast.LENGTH_SHORT).show();
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
                        Alert.showAlertDialog(errorMessage, Signup_Activity.this);
                        error.printStackTrace();
                    }
                });

                queue.add(request_json);


            }
        });
    }


    public boolean checkInternetConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting() && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {

            return true;

        } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            return false;
        } else return false;
    }

    public void setItemsForCitySpinner() {

        //String[] cities = citiesMap.values();

        final List<String> cityList = new ArrayList<>(citiesMap.values());
        cityList.add(0, "Select City");

        // Initializing an ArrayAdapter
        spinnerArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.spinner_item, cityList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        binding.spCity.setAdapter(spinnerArrayAdapter);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);

        binding.spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //deleting when the user select any other city other than he has selected before
                Paper.book().delete(selectedCity);
                Paper.book().delete("selectedLocality");

                selectedCity = cityList.get(position);

                if (!cityList.get(position).equalsIgnoreCase("select city")) {
                    localityKey.addAll(citiesMap.keySet());
                    cityId = localityKey.get(position - 1);

                    //       startActivity(new Intent(Signup_Activity.this, SearchLocality.class));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }


    public void setItemsForUserTypeSpinner(int arrayId) {

        String[] plants = getResources().getStringArray(arrayId);

        final List<String> plantsList = new ArrayList<>(Arrays.asList(plants));

        // Initializing an ArrayAdapter
        spinnerArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.spinner_item, plantsList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        binding.spUserType.setAdapter(spinnerArrayAdapter);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);

    }

    public void showOtpDialog(final Context context, final String number) {

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

                    sendOtpWithMobile(number, otp);


                } else {
                    et_otp.setError("Please enter otp first");
                }
            }
        });

        tv_resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resendOtp(number);
                tv_resendOtp.setVisibility(View.INVISIBLE);
                tv_timer.setVisibility(View.VISIBLE);
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

    private void sendOtpWithMobile(final String number, final String input) {
        final String sendOtpUrl = "http://www.admin-panel.adecity.com/api/verify-otp";

        new Handler().post(new Runnable() {
            @Override
            public void run() {

                progressDialog.setMessage("Verifying user");
                progressDialog.show();
// Post params to be sent to the server
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("mobile", number);
                params.put("otp", input);

                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, sendOtpUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                //  Toast.makeText(Signup_Activity.this, "" + response, Toast.LENGTH_SHORT).show();


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {
                                        progressDialog.dismiss();
                                        //   btnSignIn.setProgress(100);
                                        otpDialog.dismiss();
                                        new SweetAlertDialog(Signup_Activity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Congrats")
                                                .setContentText("You are successfully verified")


                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sDialog) {


                                                        //    View vv = getActivity().findViewById(R.id.frame_include);
                                                        //   getFragmentManager().beginTransaction().replace(vv.getId(), new login()).commit();

                                                        sDialog.dismissWithAnimation();
                                                        startActivity(new Intent(Signup_Activity.this, LoginActivity.class));
                                                        overridePendingTransition(R.anim.back_in, R.anim.back_out);
                                                    }
                                                })
                                                .show();

                                    } else {
                                        progressDialog.dismiss();


                                        Alert.showAlertDialog(response.getString("msg"), Signup_Activity.this);
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
                        progressDialog.dismiss();
                        String message = VollyErrors.getInstance().showVollyError(error);

                        Alert.showAlertDialog(message, Signup_Activity.this);
                    }
                });

// add the request object to the queue to be executed
                //ApplicationController.getInstance().addToRequestQueue(request_json);
                queue.add(request_json);


            }
        });


    }

    public void resendOtp(final String number) {


        final String reSendOtpUrl = "http://www.admin-panel.adecity.com/api/resend-otp";
        progressDialog.setMessage("Resending Otp");
        progressDialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("mobile", number);


                final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, reSendOtpUrl, new JSONObject(params),
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

                                                Alert.showAlertDialog("Otp sent successfully", Signup_Activity.this);
                                                progressDialog.dismiss();

                                            }
                                        }, 2000);


                                    } else {
                                        progressDialog.dismiss();


                                        Alert.showAlertDialog(response.getString("msg"), Signup_Activity.this);
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
                        String errorMessage = vollyErrors.showVollyError(error);
                        progressDialog.dismiss();
                        Alert.showAlertDialog(errorMessage, Signup_Activity.this);
                        error.printStackTrace();
                    }
                });

                queue.add(request_json);


            }
        }).start();


    }


    public void getUserCities() {


        final String getCityUrl = "http://www.admin-panel.adecity.com/api/get-city";


        new Thread(new Runnable() {
            @Override
            public void run() {


                final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, getCityUrl, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {

                                        JSONArray cityArray = response.getJSONArray("data");
                                        for (int i = 0; i < cityArray.length(); i++) {
                                            JSONObject cityObject = cityArray.getJSONObject(i);
                                            citiesMap.put(cityObject.getString("_id"), cityObject.getString("cityname"));
                                        }
                                        setItemsForCitySpinner();


                                    } else {


                                        Alert.showAlertDialog(response.getString("msg"), Signup_Activity.this);
                                        //  btnSignIn.setProgress(100);
                                    }

                                } catch (JSONException e) {

                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = vollyErrors.showVollyError(error);
                        Alert.showAlertDialog(errorMessage, Signup_Activity.this);
                        error.printStackTrace();
                    }
                });

                queue.add(request_json);


            }
        }).start();


    }
}
