package firebasecloud.com.firebasecloud.ActiveTaskFragements;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.Alert;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.TakePictureTask;
import firebasecloud.com.firebasecloud.CustomElements.TaskItems_POJO;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;

import static firebasecloud.com.firebasecloud.TaskFragments.NewTasks.getDate;

public class OnGoing extends Fragment {

    ProgressDialog progressDialog;
    RecyclerView rv_taskRecycleView;
    TaskAdapter adapter;
    RadioButton rb_pending,rb_complete;
    public static String taskId, globalTaskId;


    Handler handler;
    RequestQueue queue;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private AlertDialog dialog, moveTaskDialog;
    SweetAlertDialog mDialog;

    ArrayList<TaskItems_POJO> globalTaskList;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public OnGoing() {
        // Required empty public constructor
    }


    public static OnGoing newInstance(String param1, String param2) {
        OnGoing fragment = new OnGoing();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        progressDialog = new ProgressDialog(getActivity());
        queue = vollySingleton.getInstance().getRequestQueue();
        globalTaskList = new ArrayList<>();
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_on_going, container, false);

        adapter = new TaskAdapter(getActivity());
        rv_taskRecycleView = view.findViewById(R.id.rv_taskList);
        rv_taskRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_taskRecycleView.setAdapter(adapter);

        getTaskListForUser(getActivity());


        return view;
    }

    public void getTaskListForUser(final Context context) {

        progressDialog.setMessage("Getting On Going Task..");
        progressDialog.show();

        final String taskUrl = "http://www.admin-panel.adecity.com/task/get-active-task";
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("user_id", NewTasks.userId);
                //    data.put("type", "Unconfirmed");


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

                                                try {
                                                    globalTaskList = getListItemsAfterResponse(response);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                adapter.setSource(globalTaskList);

                                            }
                                        }, 2000);

                                    } else {

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                                Alert.showAlertDialog("No On Going task", context);

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
                        progressDialog.dismiss();

                        System.out.println(error.toString());
                        Toast.makeText(context, "" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {


                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }

                };

                queue.add(request_json);


            }
        }).start();
    }

    public ArrayList<TaskItems_POJO> getListItemsAfterResponse(JSONObject response) throws JSONException {


        ArrayList<TaskItems_POJO> taskItemsList = new ArrayList<>();
        JSONArray taskArray = response.getJSONArray("task");

        for (int i = 0; i < taskArray.length(); i++) {


            JSONObject taskObject = taskArray.getJSONObject(i);

            TaskItems_POJO taskItems = new TaskItems_POJO();

            taskId = taskObject.getString("_id");
            taskItems.setTitle(taskObject.getString("title"));
            taskItems.setDescription(taskObject.getString("desc"));
            taskItems.setStartDate(taskObject.getString("startDate"));
            taskItems.setEndDate(taskObject.getString("endDate"));
            taskItems.setStatus(taskObject.getString("status"));
            taskItems.setGlobal_taskId(taskObject.getString("global_task_id"));

            JSONObject imageCountObject = taskObject.getJSONObject("task");
            taskItems.setImagesToUpload(imageCountObject.getInt("imagesToUpload"));

            taskItems.setIncentive(taskObject.getString("incentive"));
            taskItems.setTaskId(taskId);


            taskItemsList.add(taskItems);


        }

        return taskItemsList;

    }


    class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {
        ArrayList<TaskItems_POJO> taskList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        TaskAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public TaskAdapter.TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.accept, parent, false);

            return new TaskHolder(view);
        }

        @Override
        public void onBindViewHolder(TaskAdapter.TaskHolder holder, int position) {

            TaskItems_POJO itemList = taskList.get(position);

            holder.tv_description.setText(itemList.getDescription());
            holder.tv_incentive.setText("₹" + itemList.getIncentive() + "/-");
            try {
                holder.tv_startDate.setText("Start: " + getDate(itemList.getStartDate(),false));
                holder.tv_endDate.setText("End: " + getDate(itemList.getEndDate(),false));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String status = itemList.getStatus();
            if (status.equalsIgnoreCase("live")) {
                holder.tv_status.setTextColor(getResources().getColor(R.color.colorUnreadText));
                holder.view.setBackgroundColor(getResources().getColor(R.color.colorUnreadText));

            } else if (status.equalsIgnoreCase("ended")) {
                holder.tv_status.setTextColor(Color.parseColor("#FF4500"));

                holder.view.setBackgroundColor(Color.parseColor("#FF4500"));

            } else {
                holder.tv_status.setTextColor(Color.parseColor("#F1C40F"));

                holder.view.setBackgroundColor(Color.parseColor("#F1C40F"));

            }
            holder.tv_status.setText(itemList.getStatus());


        }

        @Override
        public int getItemCount() {

            return taskList.size();
        }

        public void setSource(ArrayList<TaskItems_POJO> list) {
            if (list.size() != 0) {
                this.taskList = list;

                notifyItemRangeRemoved(0, taskList.size());

                notifyDataSetChanged();
            }

        }

        class TaskHolder extends RecyclerView.ViewHolder {
          TextView tv_description, tv_incentive, tv_startDate, tv_endDate, tv_status;
            View view;
            CardView cv_taskCard;


            public TaskHolder(View itemView) {
                super(itemView);
                tv_description = itemView.findViewById(R.id.tv_description);
                tv_incentive = itemView.findViewById(R.id.tv_task_incentive);
                tv_startDate = itemView.findViewById(R.id.tv_active_task_date);
                tv_endDate = itemView.findViewById(R.id.tv_active_end_date);
                tv_status = itemView.findViewById(R.id.tv_status);
                view = itemView.findViewById(R.id.line);

                cv_taskCard = itemView.findViewById(R.id.ongoingCard);
                cv_taskCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            String status = globalTaskList.get(getAdapterPosition()).getStatus();
                            if (status.equalsIgnoreCase("ended")) {
                                Alert.showAlertDialog("Your task has already ended. Kindly contact okhlee", context);
                            } else {

                                taskMoreInfoDialog(context, getAdapterPosition());
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                });


            }

            public void taskMoreInfoDialog(final Context context, final int taskPosition) throws ParseException {
                final AlertDialog.Builder malert = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.ongoing_dialog, null);


                final TaskItems_POJO selectedTask = globalTaskList.get(taskPosition);

                final String startDate = selectedTask.getStartDate();
                String endDate = selectedTask.getEndDate();

                String expireDate = selectedTask.getTaskExpire();


                TextView tv_cancel = dialogView.findViewById(R.id.tv_cancel);
                TextView tv_proceed = dialogView.findViewById(R.id.tv_proceed);

                TextView tv_title = dialogView.findViewById(R.id.tv_title);
                TextView tv_description = dialogView.findViewById(R.id.tv_desc);
                TextView tv_startDate = dialogView.findViewById(R.id.tv_start_date);
                TextView tv_endDate = dialogView.findViewById(R.id.tv_end_date);
                TextView tv_status = dialogView.findViewById(R.id.tv_status);

                globalTaskId = selectedTask.getGlobal_taskId();

                ImageView im_moreOptions = dialogView.findViewById(R.id.im_moreOptions);


                im_moreOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        getImageCount(context, taskPosition);
                        showMoveDialog(context, taskPosition);
                    }
                });
                tv_title.setText(selectedTask.getTitle());
                tv_description.setText(selectedTask.getDescription());
                tv_startDate.setText("Start: "+getDate(startDate,true));
                tv_endDate.setText("End: "+getDate(endDate,true));

                String status = selectedTask.getStatus();
                if (status.equalsIgnoreCase("live")) {

                    tv_status.setTextColor(getResources().getColor(R.color.colorUnreadText));
                } else if (status.equalsIgnoreCase("ended")) {
                    tv_status.setTextColor(Color.parseColor("#FF4500"));
                } else {
                    tv_status.setTextColor(Color.parseColor("#F1C40F"));

                }

                tv_status.setText("Status : " + selectedTask.getStatus());

                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        dialog.dismiss();

                    }
                });


                tv_proceed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(context, TakePictureTask.class));
                        getActivity().overridePendingTransition(R.anim.back_in, R.anim.back_out);

                        dialog.dismiss();

                    }
                });


                malert.setView(dialogView);

                dialog = malert.create();
                dialog.show();


            }

            private void showMoveDialog(final Context context, final int position) {

                int imagesToUpload=globalTaskList.get(position).getImagesToUpload();
                final AlertDialog.Builder malert = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.move_task_dialog, null);


                TextView tv_proceed = dialogView.findViewById(R.id.tv_proceed);
                TextView tv_cancel = dialogView.findViewById(R.id.tv_cancel);

                rb_pending = dialogView.findViewById(R.id.rb_pending);
                rb_complete = dialogView.findViewById(R.id.rb_complete);

                tv_proceed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (rb_pending.isChecked()) {
                            moveTask(context, position, "http://www.admin-panel.adecity.com/task/add-task-pending", "Task status changed to pending");

                        } else if (rb_complete.isChecked()) {
                            moveTask(context, position, "http://www.admin-panel.adecity.com/task/add-task-complete", "Task status changed to complete for further review");
                        } else {
                            Alert.showAlertDialog("Please select any option", context);
                        }


                    }
                });
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moveTaskDialog.dismiss();
                    }
                });


                malert.setView(dialogView);

                moveTaskDialog = malert.create();
                moveTaskDialog.show();


            }

            public void moveTask(final Context context, int taskPosition, final String url, final String msg) {

                final String task_id = globalTaskList.get(taskPosition).getTaskId();
                progressDialog.setMessage("Moving task..");
                progressDialog.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        HashMap<String, String> data = new HashMap<>();
                        data.put("user_id", NewTasks.userId);
                        data.put("task_id", task_id);


                        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(data),
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

                                                        final SweetAlertDialog mDialog = Alert.showAlertDialog(msg, context);

                                                        mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                dialog.dismiss();

                                                                moveTaskDialog.dismiss();

                                                                getTaskListForUser(getActivity());
                                                                mDialog.dismiss();

                                                            }
                                                        });
                                                       /* new SweetAlertDialog(context).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                                            }
                                                        });
*/


                                                        /*dialog.setCanceledOnTouchOutside(false);
                                                      mDialog= Alert.showAlertDialog(msg,context);
                                                      mDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                          @Override
                                                          public void onClick(SweetAlertDialog sweetAlertDialog) {

                                                              getTaskListForUser(getActivity());
                                                              dialog.dismiss();
                                                              moveTaskDialog.dismiss();
                                                          }
                                                      });*/
                                                        //    startActivity(new Intent(context, MainActivity.class));

                                                    }
                                                }, 1000);

                                            } else {

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressDialog.dismiss();
                                                        Alert.showAlertDialog("Something went wrong,try again", context);
                                                        dialog.dismiss();
                                                        moveTaskDialog.dismiss();

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
                                Alert.showAlertDialog(errorMessage, getActivity());
                                error.printStackTrace();
                            }
                        }) {


                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<String, String>();
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }

                        };

                        queue.add(request_json);


                    }
                }).start();
            }


        }

        private void getImageCount(final Context context, int taskPosition) {
            final String global_taskId = globalTaskList.get(taskPosition).getGlobal_taskId();

            final String getImageCountUrl = "http://www.admin-panel.adecity.com/task/get-user-image-count";
            new Thread(new Runnable() {
                @Override
                public void run() {

                    HashMap<String, String> data = new HashMap<>();
                    data.put("user_id", NewTasks.userId);
                    data.put("global_task_id", global_taskId);


                    JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, getImageCountUrl, new JSONObject(data),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(final JSONObject response) {


                                    try {
                                        boolean success = response.getBoolean("success");


                                        if (success) {

                                           int  totalImagesRemaining = response.getInt("count");
                                           int totalImagesToUpload = response.getInt("totalImagesToUpload");

                                            if(totalImagesRemaining!=totalImagesToUpload) {
                                                rb_complete.setEnabled(false);
                                            }


                                        } else {


                                            Alert.showAlertDialog(response.getString("msg"), context);
                                            //  btnSignIn.setProgress(100);
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
                            Alert.showAlertDialog(errorMessage, getActivity());
                            error.printStackTrace();
                        }
                    }) {


                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }

                    };

                    queue.add(request_json);


                }
            }).start();
        }


    }
}



