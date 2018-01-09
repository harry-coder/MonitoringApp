package firebasecloud.com.firebasecloud.ActiveTaskFragements;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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
import firebasecloud.com.firebasecloud.CustomElements.CustomFontTextView;
import firebasecloud.com.firebasecloud.LoginActivity;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.TakePictureTask;
import firebasecloud.com.firebasecloud.CustomElements.TaskItems_POJO;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;

public class OnGoing extends Fragment {

    ProgressDialog progressDialog;
    RecyclerView rv_taskRecycleView;
    TaskAdapter adapter;
    public static String taskId;

    Handler handler;
    RequestQueue queue;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
                data.put("user_id", LoginActivity.userId);
                //data.put("type", "Unconfirmed");


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
                holder.tv_startDate.setText("Start: " + NewTasks.getDate(itemList.getStartDate()));
                holder.tv_endDate.setText("End: " + NewTasks.getDate(itemList.getEndDate()));
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
            CustomFontTextView tv_description, tv_incentive, tv_startDate, tv_endDate, tv_status;
            View view;
            //  CardView cv_taskCard;


            public TaskHolder(View itemView) {
                super(itemView);
                tv_description = itemView.findViewById(R.id.tv_description);
                tv_incentive = itemView.findViewById(R.id.tv_task_incentive);
                tv_startDate = itemView.findViewById(R.id.tv_active_task_date);
                tv_endDate = itemView.findViewById(R.id.tv_active_end_date);
                tv_status = itemView.findViewById(R.id.tv_status);
                view = itemView.findViewById(R.id.line);

/*
                cv_taskCard = itemView.findViewById(R.id.cv_task_card);
                cv_taskCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(context, TakePictureTask.class));
                        getActivity().overridePendingTransition(R.anim.back_in, R.anim.back_out);

                    }
                });
*/


            }
        }
    }


}
