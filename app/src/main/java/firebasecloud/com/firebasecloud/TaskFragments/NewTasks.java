package firebasecloud.com.firebasecloud.TaskFragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.Alert;
import firebasecloud.com.firebasecloud.CustomElements.CustomFontTextView;
import firebasecloud.com.firebasecloud.CustomElements.TaskItems_POJO;
import firebasecloud.com.firebasecloud.LoginActivity;
import firebasecloud.com.firebasecloud.MainActivity;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;


public class NewTasks extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    SweetAlertDialog sweetAlertDialogialog;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ProgressDialog progressDialog;
    private Handler handler;
    public ArrayList<TaskItems_POJO> globalTaskList;
    private RequestQueue queue;
    public static String taskId;
    RecyclerView rv_userTaskRecycleView;
    UserTaskListAdapter adapter;
    public AlertDialog dialog = null;

    public NewTasks() {
        // Required empty public constructor
    }

    public static NewTasks newInstance(String param1, String param2) {
        NewTasks fragment = new NewTasks();
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
        handler = new Handler();
        globalTaskList = new ArrayList<>();
        queue = vollySingleton.getInstance().getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_tasks, container, false);
        rv_userTaskRecycleView = view.findViewById(R.id.rv_userTaskRecycleView);
        adapter = new UserTaskListAdapter(getActivity());

        rv_userTaskRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_userTaskRecycleView.setAdapter(adapter);

        getTaskListForUser(getActivity());


        return view;
    }


    public void getTaskListForUser(final Context context) {

        progressDialog.setMessage("Getting On Going Task..");
        progressDialog.show();

        final String taskUrl = "http://www.admin-panel.adecity.com/task/get-task";
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("user_id", LoginActivity.userId);
                data.put("type", "Unconfirmed");


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, taskUrl, new JSONObject(data),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject response) {

                                System.out.println(response);

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
            taskItems.setIncentive(taskObject.getString("incentive"));
            taskItems.setEndDate(taskObject.getString("endDate"));
            taskItems.setTaskExpire(taskObject.getString("taskExpires"));
            taskItems.setTaskId(taskId);


            taskItemsList.add(taskItems);


        }

        return taskItemsList;

    }

    public class UserTaskListAdapter extends RecyclerView.Adapter<UserTaskListAdapter.TaskHolder> {

        ArrayList<TaskItems_POJO> taskList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        UserTaskListAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public UserTaskListAdapter.TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.ongoing_singleitem, parent, false);

            return new UserTaskListAdapter.TaskHolder(view);
        }

        @Override
        public void onBindViewHolder(UserTaskListAdapter.TaskHolder holder, int position) {

            TaskItems_POJO itemList = taskList.get(position);

            holder.tv_title.setText(itemList.getTitle());
            holder.tv_incentive.setText("₹" + itemList.getIncentive() + "/-");
            try {
                holder.tv_startDate.setText("Start Date: " + getDate(itemList.getStartDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }


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

            CustomFontTextView tv_title, tv_incentive, tv_startDate;
            CardView cv_taskCard;


            public TaskHolder(View itemView) {
                super(itemView);
                tv_title = itemView.findViewById(R.id.tv_title);
                tv_incentive = itemView.findViewById(R.id.tv_incentive);
                tv_startDate = itemView.findViewById(R.id.tv_start_date);

                cv_taskCard = itemView.findViewById(R.id.cv_task_card);
                cv_taskCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            AcceptRejectDialog(context, getAdapterPosition());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                });


            }
        }



        public void AcceptRejectDialog(final Context context, int taskPosition) throws ParseException {
            final AlertDialog.Builder malert = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.accept_reject_dialog, null);


            final TaskItems_POJO selectedTask = globalTaskList.get(taskPosition);

            final String startDate = selectedTask.getStartDate();
            String endDate = selectedTask.getEndDate();

            String expireDate = selectedTask.getTaskExpire();


            TextView tv_reject = dialogView.findViewById(R.id.tv_reject);
            TextView tv_accept = dialogView.findViewById(R.id.tv_accept);

            TextView tv_title = dialogView.findViewById(R.id.tv_title);
            TextView tv_description = dialogView.findViewById(R.id.tv_desc);
            TextView tv_startDate = dialogView.findViewById(R.id.tv_start_date);
            TextView tv_endDate = dialogView.findViewById(R.id.tv_end_date);
            TextView tv_expireDate = dialogView.findViewById(R.id.tv_expire);


            tv_title.setText(selectedTask.getTitle());
            tv_description.setText(selectedTask.getDescription());
            tv_startDate.setText(getDate(startDate));
            tv_endDate.setText(getDate(endDate));
            tv_expireDate.setText("Expire : " + getDate(expireDate));

            tv_reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TaskConfirmation(false, selectedTask.getTaskId(), startDate);

                    dialog.dismiss();

                }
            });


            tv_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //    String reason = et_reason.getText().toString();
                    TaskConfirmation(true, selectedTask.getTaskId(), startDate);

                    dialog.dismiss();

                }
            });


            malert.setView(dialogView);

            dialog = malert.create();
            dialog.show();


        }

        private void TaskConfirmation(final boolean isAccepted, final String taskId, final String date) {
            progressDialog.setMessage("Updating task list");
            progressDialog.show();

            final String taskUrl = "http://www.admin-panel.adecity.com/task/task-action";
            new Thread(new Runnable() {
                @Override
                public void run() {

                    HashMap<String, String> data = new HashMap<>();
                    data.put("user_id", LoginActivity.userId);
                    data.put("isAccepted", String.valueOf(isAccepted));
                    data.put("task_id", taskId);
                    data.put("date", date);

                    System.out.println("isAccepted "+isAccepted);
                    System.out.println("date "+date);

                    JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, taskUrl, new JSONObject(data),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(final JSONObject response) {

                                    System.out.println(response);

                                    try {
                                        boolean success = response.getBoolean("success");


                                        if (success) {
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.dismiss();

                                                    if (isAccepted) {
                                                        sweetAlertDialogialog = Alert.showAlertDialog("Thanks for Accepting this task. Your task has been added to Accepted section", getActivity());
                                                       sweetAlertDialogialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                startActivity(new Intent(getActivity(), MainActivity.class));

                                                            }
                                                        });
                                                    } else {

                                                 sweetAlertDialogialog=Alert.showAlertDialog("You have rejected this task. Your task has been added to Rejected section", getActivity());
                                                        sweetAlertDialogialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                            @Override
                                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                startActivity(new Intent(getActivity(), MainActivity.class));

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
                            Alert.showAlertDialog(errorMessage, getActivity());
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
    public static String getDate(String date) throws ParseException {
        if (date != null) {
            String newdate[] = date.split("T");

            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(newdate[0]);

            return DateFormat.getDateInstance().format(date1);
        } else {
            return null;
        }
    }


}
