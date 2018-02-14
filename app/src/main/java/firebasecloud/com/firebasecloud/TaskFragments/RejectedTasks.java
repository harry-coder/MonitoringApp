package firebasecloud.com.firebasecloud.TaskFragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

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

import firebasecloud.com.firebasecloud.Alert;
import firebasecloud.com.firebasecloud.CustomElements.RejectedTaskPojo;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;


public class RejectedTasks extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ProgressDialog progressDialog;
    Handler handler;
    ArrayList<RejectedTaskPojo> globalAcceptedTaskList;
    RequestQueue queue;
    public String taskId;
    UserRejectedTaskListAdapter adapter;
    RecyclerView userRejectedTaskRecycleView;
    ViewFlipper flipper;


    public RejectedTasks() {
        // Required empty public constructor
    }

    public static RejectedTasks newInstance(String param1, String param2) {
        RejectedTasks fragment = new RejectedTasks();
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
        globalAcceptedTaskList = new ArrayList<>();
        queue = vollySingleton.getInstance().getRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_rejected_tasks, container, false);

        userRejectedTaskRecycleView = view.findViewById(R.id.rv_userRejectedTaskRecycleView);
        adapter = new UserRejectedTaskListAdapter(getActivity());
        userRejectedTaskRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

        flipper=view.findViewById(R.id.flipper);
        userRejectedTaskRecycleView.setAdapter(adapter);

      //  getRejectedTaskListForUser(getActivity());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getRejectedTaskListForUser(getActivity());

    }

    public void getRejectedTaskListForUser(final Context context) {

       progressDialog.setMessage("Getting Task..");
        progressDialog.show();

        final String taskUrl = "http://www.admin-panel.adecity.com/task/get-task";
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("user_id", NewTasks.userId);
                data.put("type", "Rejected");


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
                                                    globalAcceptedTaskList = getListItemsAfterResponse(response);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                adapter.setSource(globalAcceptedTaskList);

                                            }
                                        }, 2000);

                                    } else {

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                                flipper.showNext();
                                           //     Alert.showAlertDialog("No Rejected task", context);

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

    public ArrayList<RejectedTaskPojo> getListItemsAfterResponse(JSONObject response) throws JSONException {

        ArrayList<RejectedTaskPojo> taskItemsList = new ArrayList<>();
        JSONArray taskArray = response.getJSONArray("task");

        for (int i = 0; i < taskArray.length(); i++) {


            JSONObject taskObject = taskArray.getJSONObject(i);

            RejectedTaskPojo taskItems =new  RejectedTaskPojo();

            taskId = taskObject.getString("_id");
            taskItems.setTitle(taskObject.getString("title"));
            taskItems.setDescription(taskObject.getString("desc"));
            taskItems.setStartDate(taskObject.getString("startDate"));
            taskItems.setIncentive(taskObject.getString("incentive"));
            taskItems.setEndDate(taskObject.getString("endDate"));
            taskItems.setStatus(taskObject.getString("status"));

            taskItems.setTaskExpire(taskObject.getString("taskExpires"));
            taskItems.setTaskId(taskId);


            taskItemsList.add(taskItems);


        }

        return taskItemsList;

    }

    public class UserRejectedTaskListAdapter extends RecyclerView.Adapter<UserRejectedTaskListAdapter.TaskHolder> {

        ArrayList<RejectedTaskPojo> taskList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        UserRejectedTaskListAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public UserRejectedTaskListAdapter.TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.rejected_single_item, parent, false);

            return new UserRejectedTaskListAdapter.TaskHolder(view);
        }

        @Override
        public void onBindViewHolder(UserRejectedTaskListAdapter.TaskHolder holder, int position) {

            RejectedTaskPojo itemList = taskList.get(position);

            String status=itemList.getStatus();
            holder.tv_description.setText(itemList.getTitle());
            holder.tv_incentive.setText("₹"+itemList.getIncentive()+"/-");
            try {
                holder.tv_endDate.setText("End: " + NewTasks.getDate(itemList.getEndDate(),false));

                holder.tv_startDate.setText("Start: " + NewTasks.getDate(itemList.getStartDate(),false));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(status.equalsIgnoreCase("expired"))
            {
                GradientDrawable drawable = (GradientDrawable)holder.tv_status.getBackground();
                drawable.setStroke(1, Color.parseColor("#F1C40F"));
                holder.tv_status.setTextColor(Color.parseColor("#F1C40F"));
                holder.view.setBackgroundColor(getResources().getColor(R.color.expiredColor));
            }
            else
            {
                GradientDrawable drawable = (GradientDrawable)holder.tv_status.getBackground();
                drawable.setStroke(1, Color.parseColor("#FF4500"));
                holder.view.setBackgroundColor(getResources().getColor(R.color.rejectedColor));
            }
            holder.tv_status.setText(status);


        }

        @Override
        public int getItemCount() {

            return taskList.size();
        }

        public void setSource(ArrayList<RejectedTaskPojo> list) {
            if (list.size() != 0) {
                this.taskList = list;

                notifyItemRangeRemoved(0, taskList.size());

                notifyDataSetChanged();
            }

        }

        class TaskHolder extends RecyclerView.ViewHolder {

            TextView tv_description, tv_endDate, tv_startDate,tv_incentive,tv_status;
            View view;
//            CardView cv_taskCard;


            public TaskHolder(View itemView) {
                super(itemView);
                tv_description = itemView.findViewById(R.id.tv_description);
                tv_endDate = itemView.findViewById(R.id.tv_end_date);
                tv_startDate = itemView.findViewById(R.id.tv_start_date);

                view=itemView.findViewById(R.id.view);
                tv_status=itemView.findViewById(R.id.tv_status);
                tv_incentive=itemView.findViewById(R.id.tv_incentive);
               /* cv_taskCard = itemView.findViewById(R.id.cv_task_card);
                cv_taskCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                    }
                });*/



            }
        }


    }


}
