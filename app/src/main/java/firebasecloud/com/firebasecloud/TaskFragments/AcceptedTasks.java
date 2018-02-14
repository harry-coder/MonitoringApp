package firebasecloud.com.firebasecloud.TaskFragments;


import android.app.ProgressDialog;
import android.content.Context;
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
import firebasecloud.com.firebasecloud.CustomElements.AcceptedTaskPojo;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;


public class AcceptedTasks extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ProgressDialog progressDialog;
    Handler handler;
    ArrayList<AcceptedTaskPojo> globalAcceptedTaskList;
    RequestQueue queue;
    public String taskId;
    UserAcceptedTaskListAdapter adapter;
    RecyclerView userAccptedTaskRecycleView;

    ViewFlipper flipper;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public AcceptedTasks() {
        // Required empty public constructor
    }

    public static AcceptedTasks newInstance(String param1, String param2) {
        AcceptedTasks fragment = new AcceptedTasks();
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
        View view = inflater.inflate(R.layout.fragment_accepted_tasks, container, false);

        userAccptedTaskRecycleView = view.findViewById(R.id.rv_userAcceptedTaskRecycleView);
        adapter = new UserAcceptedTaskListAdapter(getActivity());
        userAccptedTaskRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userAccptedTaskRecycleView.setAdapter(adapter);

        flipper=view.findViewById(R.id.flipper);

      //  getAccptedTaskListForUser(getActivity());
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        getAccptedTaskListForUser(getActivity());

    }

    public void getAccptedTaskListForUser(final Context context) {

        progressDialog.setMessage("Getting  Task..");
        progressDialog.show();

        final String taskUrl = "http://www.admin-panel.adecity.com/task/get-task";
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("user_id", NewTasks.userId);
                data.put("type", "Upcoming");


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, taskUrl, new JSONObject(data),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(final JSONObject response) {

                                System.out.println(response);

                                try {
                                    boolean success = response.getBoolean("success");
                                    progressDialog.dismiss();


                                    if (success) {
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

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

    public ArrayList<AcceptedTaskPojo> getListItemsAfterResponse(JSONObject response) throws JSONException {

        ArrayList<AcceptedTaskPojo> taskItemsList = new ArrayList<>();
        JSONArray taskArray = response.getJSONArray("task");

        for (int i = 0; i < taskArray.length(); i++) {


            JSONObject taskObject = taskArray.getJSONObject(i);

            AcceptedTaskPojo taskItems = new AcceptedTaskPojo();

            taskId = taskObject.getString("_id");
            taskItems.setTitle(taskObject.getString("title"));
            taskItems.setDescription(taskObject.getString("desc"));
            taskItems.setStartDate(taskObject.getString("startDate"));
            taskItems.setIncentive(taskObject.getString("incentive"));
            taskItems.setStatus(taskObject.getString("status"));
            taskItems.setEndDate(taskObject.getString("endDate"));
            taskItems.setTaskExpire(taskObject.getString("taskExpires"));
            taskItems.setTaskId(taskId);


            taskItemsList.add(taskItems);


        }

        return taskItemsList;

    }

    public class UserAcceptedTaskListAdapter extends RecyclerView.Adapter<UserAcceptedTaskListAdapter.TaskHolder> {

        ArrayList<AcceptedTaskPojo> taskList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        UserAcceptedTaskListAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public UserAcceptedTaskListAdapter.TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            View view = inflater.inflate(R.layout.accepted_single_item, parent, false);

            return new UserAcceptedTaskListAdapter.TaskHolder(view);
        }

        @Override
        public void onBindViewHolder(UserAcceptedTaskListAdapter.TaskHolder holder, int position) {

            AcceptedTaskPojo itemList = taskList.get(position);

            holder.tv_description.setText(itemList.getDescription());
            holder.tv_incentive.setText("₹"+itemList.getIncentive()+"/-");

            holder.tv_status.setText(itemList.getStatus());

            try {
                holder.tv_endDate.setText("End: " + NewTasks.getDate(itemList.getEndDate(),false));

                holder.tv_startDate.setText("Start : " + NewTasks.getDate(itemList.getStartDate(),false));
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

        @Override
        public int getItemCount() {

            return taskList.size();
        }

        public void setSource(ArrayList<AcceptedTaskPojo> list) {
            if (list.size() != 0) {
                this.taskList = list;

                notifyItemRangeRemoved(0, taskList.size());

                notifyDataSetChanged();
            }

        }

        class TaskHolder extends RecyclerView.ViewHolder {

            TextView tv_description, tv_endDate, tv_startDate,tv_incentive,tv_status;
//            CardView cv_taskCard;


            public TaskHolder(View itemView) {
                super(itemView);
                tv_description = itemView.findViewById(R.id.tv_description);
                tv_endDate = itemView.findViewById(R.id.tv_end_date);
                tv_startDate = itemView.findViewById(R.id.tv_start_date);
                tv_incentive = itemView.findViewById(R.id.tv_incentive);

                tv_status=itemView.findViewById(R.id.tv_status);
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
