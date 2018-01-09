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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.databinding.ActivitySignupBinding;
import io.paperdb.Paper;

public class Signup_Activity extends AppCompatActivity {

    ActivitySignupBinding binding;
    ProgressDialog progressDialog;
    RequestQueue queue;
    String mobile, password, passwordConfirm,userName;
    public static String city,userType;
     ArrayAdapter<String> spinnerArrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup_);

        progressDialog = new ProgressDialog(this);

        queue = vollySingleton.getInstance().getRequestQueue();

        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkInternetConnection()) {
                    if (autheticateDetails()) {
                        signupWithCredentials(mobile,password,userName);
                    }
                }
                else LoginActivity.netConnectivityDialog(Signup_Activity.this);

            }
        });

        binding.tvSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Signup_Activity.this,LoginActivity.class));
                overridePendingTransition(R.anim.back_in, R.anim.back_out);

            }
        });



        setItemsForCitySpinner(R.array.city_type);
        setItemsForUserTypeSpinner(R.array.user_type);





    }

    public boolean autheticateDetails() {
        mobile = binding.mobilenumber.getText().toString();
        password = binding.password.getText().toString();
        passwordConfirm = binding.confirmPassword.getText().toString();
        userName=binding.name.getText().toString();
        city= (String) binding.spCity.getSelectedItem();
        userType= (String) binding.spUserType.getSelectedItem();

        System.out.println("City "+city);
        System.out.println("usertype "+userType);


        if (TextUtils.isEmpty(mobile)) {
            binding.mobilenumber.setError(getString(R.string.provide_contact));
            binding.mobilenumber.requestFocus();
            return false;

        } else if (TextUtils.isEmpty(password)) {
            binding.password.setError(getString(R.string.field_empty));
            binding. password.requestFocus();
            return false;

        } else if (TextUtils.isEmpty(passwordConfirm)) {
            binding.confirmPassword.setError(getString(R.string.confirmpassword));
           binding. confirmPassword.requestFocus();
            return false;

        } else if (!password.equalsIgnoreCase(passwordConfirm)) {
            binding.confirmPassword.setError(getString(R.string.password_didntmatch));
            binding.confirmPassword.requestFocus();

            return false;
        }
        else if (TextUtils.isEmpty(userName)) {
            binding.name.setError(getString(R.string.enter_name));
            binding.name.requestFocus();

            return false;
        }
        else if (TextUtils.isEmpty(city)) {
           binding.tvEmptyCityWarning.setVisibility(View.VISIBLE);

           binding.tvEmptyCityWarning.setTextColor(Color.RED);
            binding.spCity.requestFocus();

            return false;
        }
        else if (TextUtils.isEmpty(userType)) {
            binding.tvEmptyWarning.setVisibility(View.VISIBLE);

            binding.tvEmptyWarning.setTextColor(Color.RED);
            binding.spUserType.requestFocus();

            return false;
        }
        return true;
    }


    public void signupWithCredentials( final String mobile, final String password,final String name) {
        final String signupUrl = "http://www.admin-panel.adecity.com/api/register";

        System.out.println("User"+city);
        System.out.println("city"+userType);
        System.out.println("name"+name);
        System.out.println("mobile"+mobile);
        System.out.println("password"+password);


        new Handler().post(new Runnable() {
            @Override
            public void run() {

                progressDialog.setMessage("Signing in.. ");
                progressDialog.show();
                HashMap<String, Object> params = new HashMap<>();
                params.put("mobile", mobile);
                params.put("password", password);

                params.put("user_type", userType);
                params.put("name", name);

                params.put("user_city", city);


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, signupUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {
                                        progressDialog.dismiss();
                                        // btnSignIn.setProgress(100);


                                        Toast.makeText(Signup_Activity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Signup_Activity.this,LoginActivity.class));
                                        overridePendingTransition(R.anim.back_in, R.anim.back_out);
                                    } else {

                                        String message = response.getString("msg");

                                        progressDialog.dismiss();
                                        Alert.showAlertDialog(message, Signup_Activity.this);

                                        // btnSignIn.setProgress(100);
                                        binding.btnSignIn.setText(R.string.tryagain);
                                        binding.btnSignIn.setBackgroundColor(Color.RED);

                                    }

                                } catch (JSONException e) {
                                    Toast.makeText(Signup_Activity.this, ""+e, Toast.LENGTH_SHORT).show();
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

    public void setItemsForCitySpinner(int arrayId) {

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
       binding.spCity.setAdapter(spinnerArrayAdapter);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);

    }


}
