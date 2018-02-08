package firebasecloud.com.firebasecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.CustomElements.TaskItems_POJO;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;

import static firebasecloud.com.firebasecloud.TaskFragments.NewTasks.getDate;

public class DialogThemedActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private Handler handler;
    private RequestQueue queue;
    public SweetAlertDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_themed);


        queue = vollySingleton.getInstance().getRequestQueue();
        handler = new Handler();
        progressDialog = new ProgressDialog(this);


        try {
            AcceptRejectDialog(this, NewTasks.specificTaskPosition);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void AcceptRejectDialog(final Context context, int taskPosition) throws ParseException {


        final TaskItems_POJO selectedTask = NewTasks.globalTaskList.get(taskPosition);

        final String startDate = selectedTask.getStartDate();
        String endDate = selectedTask.getEndDate();

        String expireDate = selectedTask.getTaskExpire();


        TextView tv_reject = findViewById(R.id.tv_reject);
        TextView tv_accept = findViewById(R.id.tv_accept);

        TextView tv_title = findViewById(R.id.tv_title);
        TextView tv_description = findViewById(R.id.tv_desc);
        TextView tv_startDate = findViewById(R.id.tv_start_date);
        TextView tv_endDate = findViewById(R.id.tv_end_date);
        TextView tv_expireDate = findViewById(R.id.tv_expire);


        tv_title.setText(selectedTask.getTitle());
        tv_description.setText(selectedTask.getDescription());
        tv_startDate.setText(getDate(startDate,false));
        tv_endDate.setText(getDate(endDate,false));
        tv_expireDate.setText("Expire : " + getDate(expireDate,false));

        tv_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TaskConfirmation(context, false, selectedTask.getTaskId(), startDate);


            }
        });


        tv_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //    String reason = et_reason.getText().toString();
                TaskConfirmation(context, true, selectedTask.getTaskId(), startDate);


            }
        });


    }

    private void TaskConfirmation(final Context context, final boolean isAccepted, final String taskId, final String date) {
        progressDialog.setMessage("Updating task list");
        progressDialog.show();

        final String taskUrl = "http://www.admin-panel.adecity.com/task/task-action";
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("user_id", NewTasks.userId);
                data.put("isAccepted", String.valueOf(isAccepted));
                data.put("task_id", taskId);
                data.put("date", date);

                System.out.println("isAccepted " + isAccepted);
                System.out.println("date " + date);

                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, taskUrl, new JSONObject(data),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();

                                                if (isAccepted) {
                                                    mDialog = Alert.showAlertDialog("Thanks for Accepting this task. Your task has been added to Accepted section", context);
                                                    mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            //           startActivity(new Intent(getActivity(), MainActivity.class));
                                                            //     getTaskListForUser(context);


                                                            mDialog.dismiss();
                                                            finish();
                                                        }
                                                    });
                                                } else {

                                                    mDialog = Alert.showAlertDialog("You have rejected this task. Your task has been added to Rejected section", context);
                                                    mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            //                 startActivity(new Intent(getActivity(), MainActivity.class));
                                                            //   getTaskListForUser(context);

                                                            mDialog.dismiss();
                                                            finish();
                                                        }
                                                    });
                                                }


                                            }
                                        }, 1000);

                                    } else {

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                                Alert.showAlertDialog("Something went wrong,try again", context);

                                            }
                                        });

                                        // btnSignIn.setProgress(100);

                                    }

                                } catch (JSONException e) {
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
                }); /*{


                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/x-www-form-urlencoded");
                            return headers;
                        }

                    };*/

                queue.add(request_json);


            }
        }).start();

    }

}


