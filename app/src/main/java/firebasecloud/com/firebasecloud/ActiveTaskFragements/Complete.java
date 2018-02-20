package firebasecloud.com.firebasecloud.ActiveTaskFragements;


import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ViewFlipper;

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
import firebasecloud.com.firebasecloud.CustomElements.CompleteTaskPojo;
import firebasecloud.com.firebasecloud.CustomElements.VollyErrors;
import firebasecloud.com.firebasecloud.LoginActivity;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;

import static firebasecloud.com.firebasecloud.TaskFragments.NewTasks.getDate;


public class Complete extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ProgressDialog progressDialog;
    RecyclerView rv_completeTaskRecycleView;
    CompleteTaskAdapter adapter;
    public ArrayList<CompleteTaskPojo> globalCompleteTaskList;

    ViewFlipper flipper;
    Handler handler;
    RequestQueue queue;


    public Complete() {
        // Required empty public constructor
    }

    public static Complete newInstance(String param1, String param2) {
        Complete fragment = new Complete();
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
        globalCompleteTaskList = new ArrayList<>();
        handler = new Handler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_complete, container, false);

        adapter = new CompleteTaskAdapter(getActivity());
        rv_completeTaskRecycleView = view.findViewById(R.id.rv_completeTaskRecycleView);
        rv_completeTaskRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_completeTaskRecycleView.setAdapter(adapter);

        flipper = view.findViewById(R.id.flipper);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getCompleteTaskListForUser(getActivity());


    }

    public void getCompleteTaskListForUser(final Context context) {

        progressDialog.setMessage("Getting On Completed and approved Task..");
        progressDialog.show();

        final String taskUrl = "http://www.admin-panel.adecity.com/task/get-task";
        new Thread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data = new HashMap<>();
                data.put("user_id", NewTasks.userId);
                data.put("type", "Complete");


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
                                                    globalCompleteTaskList = getListItemsAfterResponse(response);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                adapter.setSource(globalCompleteTaskList);

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
                        String message = VollyErrors.getInstance().showVollyError(error);

                        progressDialog.dismiss();

                        Alert.showAlertDialog(message, context);
                        error.printStackTrace();                    }
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

    public ArrayList<CompleteTaskPojo> getListItemsAfterResponse(JSONObject response) throws JSONException {

        ArrayList<CompleteTaskPojo> taskItemsList = new ArrayList<>();
        JSONArray taskArray = response.getJSONArray("task");

        for (int i = 0; i < taskArray.length(); i++) {


            JSONObject taskObject = taskArray.getJSONObject(i);

            CompleteTaskPojo taskItems = new CompleteTaskPojo();

            taskItems.setTitle(taskObject.getString("title"));
            taskItems.setStartDate(taskObject.getString("startDate"));
            taskItems.setEndDate(taskObject.getString("endDate"));
            taskItems.setStatus(taskObject.getString("status"));

            taskItems.setIncentive(taskObject.getInt("incentive"));


            taskItemsList.add(taskItems);


        }

        return taskItemsList;

    }


    class CompleteTaskAdapter extends RecyclerView.Adapter<CompleteTaskAdapter.TaskHolder> {
        ArrayList<CompleteTaskPojo> taskList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        CompleteTaskAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public CompleteTaskAdapter.TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.accepted_single_item, parent, false);

            return new CompleteTaskAdapter.TaskHolder(view);
        }

        @Override
        public void onBindViewHolder(CompleteTaskAdapter.TaskHolder holder, int position) {

            CompleteTaskPojo itemList = taskList.get(position);

            holder.tv_incentive.setText("₹" + itemList.getIncentive() + "/-");
            holder.tv_description.setText(itemList.getTitle());
            try {
                holder.tv_startDate.setText("Start: " + getDate(itemList.getStartDate(), false));
                holder.tv_endDate.setText("End: " + getDate(itemList.getEndDate(), false));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String status = itemList.getStatus();
            if (status.equalsIgnoreCase("complete")) {
                holder.tv_status.setTextColor(getResources().getColor(R.color.colorUnreadText));
                holder.view.setBackgroundColor(getResources().getColor(R.color.colorUnreadText));

            } else if (status.equalsIgnoreCase("under review")) {
                holder.tv_status.setTextColor(Color.parseColor("#FF4500"));

                holder.view.setBackgroundColor(Color.parseColor("#FF4500"));

            }
            holder.tv_status.setText(itemList.getStatus());


        }

        @Override
        public int getItemCount() {

            return taskList.size();
        }

        public void setSource(ArrayList<CompleteTaskPojo> list) {
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
                tv_incentive = itemView.findViewById(R.id.tv_incentive);
                tv_startDate = itemView.findViewById(R.id.tv_start_date);
                tv_endDate = itemView.findViewById(R.id.tv_end_date);
                tv_status = itemView.findViewById(R.id.tv_status);
                view = itemView.findViewById(R.id.view);

                cv_taskCard = itemView.findViewById(R.id.ongoingCard);
              /*  cv_taskCard.setOnClickListener(new View.OnClickListener() {
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
*/

            }


        }
    }
}
