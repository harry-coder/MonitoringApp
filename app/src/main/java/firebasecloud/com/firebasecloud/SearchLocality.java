package firebasecloud.com.firebasecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collection;
import java.util.HashMap;

import firebasecloud.com.firebasecloud.ActiveTaskFragements.Complete;
import firebasecloud.com.firebasecloud.CustomElements.ClusterLocalityName;
import firebasecloud.com.firebasecloud.CustomElements.ClusterNames;
import firebasecloud.com.firebasecloud.CustomElements.CompleteTaskPojo;
import firebasecloud.com.firebasecloud.CustomElements.Localities;
import firebasecloud.com.firebasecloud.CustomElements.TaskItems_POJO;
import firebasecloud.com.firebasecloud.CustomElements.VollyErrors;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import io.paperdb.Paper;

import static firebasecloud.com.firebasecloud.TaskFragments.NewTasks.getDate;

public class SearchLocality extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ProgressDialog progressDialog;
    VollyErrors vollyErrors;
    RequestQueue queue;

    RecyclerView localityRecycleView;
    RecyclerView rv_selectedLocality;
    RecyclerView rv_cluster;
    RecyclerView rv_clusterLocalityName;

    SearchView localitySearch;

    LocalityAdapter adapter;
    ClusterAdapter clusterAdapter;
    SelectedLocalityAdapter selectedLocalityAdapter;
    ClusterAdapter.ClusterHolder.ClusterLocalitiesAdapter clusterLocalitiesAdapter;

    public static ArrayList<Localities> selectedLocalities;
    private SparseBooleanArray selectedItems;

    TextView tv_cancel, tv_submit;

    AlertDialog dialog;

    ArrayList<ClusterLocalityName> globalClusterLocalityNamesList;
    ArrayList<String> localityAddedOnHorizontalRecycleView;
    public static ArrayList<ClusterNames> globalClusterNames;
    ArrayList<Localities> globalLocalityArrayList;
    ArrayList<String> horizontalList;
    public static ArrayList<String>[] clusterLocalityNames;


    public static String selectedLocality, clusterName;
    int clusterPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_locality);

        localitySearch = findViewById(R.id.sv_searchView);
        localitySearch.setQueryHint("Select Locality for city ");
        localitySearch.setBackgroundColor(Color.parseColor("#e927a4d1"));
        localitySearch.setOnQueryTextListener(this);


        tv_cancel = findViewById(R.id.tv_cancel);
        tv_submit = findViewById(R.id.tv_submit);


        globalClusterNames = new ArrayList<>();
        horizontalList = new ArrayList<String>();
        globalLocalityArrayList = new ArrayList<>();
        globalClusterLocalityNamesList = new ArrayList<>();


        //cluster recycleview
        rv_cluster = findViewById(R.id.rv_cluster);
        rv_cluster.setLayoutManager(new LinearLayoutManager(this));
        clusterAdapter = new ClusterAdapter(this);
        rv_cluster.setAdapter(clusterAdapter);


        queue = vollySingleton.getInstance().getRequestQueue();
        progressDialog = new ProgressDialog(this);

        //datasource for horizontal view;
        if (Paper.book().exist(Signup_Activity.selectedCity)) {

            selectedLocalities = new ArrayList<>();
            selectedLocalities = Paper.book().read(Signup_Activity.selectedCity);
            horizontalList = Paper.book().read(Signup_Activity.selectedCity);
        } else {
            selectedLocalities = new ArrayList<>();

        }


        //this stores the position of  selected items
        selectedItems = new SparseBooleanArray();


        selectedItems = new SparseBooleanArray();
        rv_selectedLocality = findViewById(R.id.rv_selectedLocality);
        rv_selectedLocality.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedLocalityAdapter = new SelectedLocalityAdapter(this);
        rv_selectedLocality.setAdapter(selectedLocalityAdapter);


        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();


            }
        });

        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Paper.book().write(Signup_Activity.selectedCity, selectedLocalities);
                //    Paper.book().write("selectedPosition", selectedItems);

                Paper.book().write("selectedLocality", localityAddedOnHorizontalRecycleView);
                finish();
            }
        });

        //source that contain selected locality name
        localityAddedOnHorizontalRecycleView = new ArrayList<>();

        if (Paper.book().exist("selectedLocality")) {

            horizontalList = Paper.book().read("selectedLocality");

        } else {
            localityAddedOnHorizontalRecycleView = new ArrayList<>();
        }


        localityRecycleView = findViewById(R.id.rv_recycleViewSearchItem);

        vollyErrors = VollyErrors.getInstance();

        adapter = new LocalityAdapter(this);
        localityRecycleView.setLayoutManager(new LinearLayoutManager(this));
        localityRecycleView.setAdapter(adapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocality(Signup_Activity.cityId, this);
        clusterLocalityNames = new ArrayList[globalClusterNames.size()];

    }


    public void getLocality(final String cityId, final Context context) {
        final String getCityUrl = "http://www.admin-panel.adecity.com/api/get-locality";

        progressDialog.setMessage("Getting localities near you");
        progressDialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("city_id", cityId);

                final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, getCityUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {
                                        progressDialog.dismiss();

                                        globalLocalityArrayList = retrieveLocalityNames(response);
                                        globalClusterNames = getClusterNames(response);


                                        adapter.setSource(globalLocalityArrayList);

                                        clusterAdapter.setSelectedSource(globalClusterNames);
                                    } else {
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
                        String errorMessage = vollyErrors.showVollyError(error);
                        progressDialog.dismiss();
                        Alert.showAlertDialog(errorMessage, context);
                        error.printStackTrace();
                    }
                });

                queue.add(request_json);


            }
        }).start();


    }

    public ArrayList<ClusterNames> getClusterNames(JSONObject response) throws JSONException {
        ArrayList<ClusterNames> clusterList = new ArrayList<>();
        if (response != null && response.length() != 0) {
            JSONArray clusterArray = response.getJSONArray("all_groups");

            for (int i = 0; i < clusterArray.length(); i++) {
                JSONObject clusterObject = clusterArray.getJSONObject(i);

                ClusterNames cluster_name = new ClusterNames();
                cluster_name.setClusterName(clusterObject.getString("name"));

                clusterList.add(cluster_name);

            }

        }
        return clusterList;
    }

    private ArrayList<Localities> retrieveLocalityNames(JSONObject response) throws JSONException {

        ArrayList<Localities> localityList = new ArrayList<>();
        if (response != null && response.length() != 0) {
            JSONArray localitiesArray = response.getJSONArray("data");

            for (int i = 0; i < localitiesArray.length(); i++) {
                JSONObject localitiesObject = localitiesArray.getJSONObject(i);

                Localities locality = new Localities();

                locality.setLocalityName(localitiesObject.getString("name"));

                localityList.add(locality);

            }


        }

        return localityList;


    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();

        ArrayList<Localities> filteredList = new ArrayList<>();
        for (Localities localities : globalLocalityArrayList) {
            String locality = localities.getLocalityName().toLowerCase();

            if (locality.equalsIgnoreCase(newText) || locality.contains(newText)) {
                filteredList.add(localities);
            }
        }
        adapter.setSource(filteredList);

        return true;
    }

    class LocalityAdapter extends RecyclerView.Adapter<LocalityAdapter.LocalityHolder> {
        ArrayList<Localities> localityList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        LocalityAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public LocalityAdapter.LocalityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.search_locality_single_item, parent, false);

            return new LocalityAdapter.LocalityHolder(view);
        }

        @Override
        public void onBindViewHolder(final LocalityAdapter.LocalityHolder holder, final int position) {

            Localities locality = localityList.get(position);

            selectedLocality = locality.getLocalityName();

            holder.tv_locality.setText(selectedLocality);

            if (selectedItems.get(position)) {
                if (localityAddedOnHorizontalRecycleView.contains(selectedLocality)) {
                    holder.tv_locality.setBackgroundColor(Color.parseColor("#e927a4d1"));
                    holder.tv_locality.setEnabled(false);

                }
            } else {
                holder.tv_locality.setEnabled(true);
                holder.tv_locality.setBackgroundColor(Color.parseColor("#ffffff"));
            }

        }


        @Override
        public int getItemCount() {

            return localityList.size();
        }

        public void setSource(ArrayList<Localities> list) {
            if (list.size() != 0) {
                this.localityList = list;

                notifyItemRangeRemoved(0, localityList.size());

                notifyDataSetChanged();
            }

        }

        class LocalityHolder extends RecyclerView.ViewHolder {
            TextView tv_locality;


            public LocalityHolder(View itemView) {
                super(itemView);
                tv_locality = itemView.findViewById(R.id.tv_locality);

                if (selectedLocalities != null) {
                    rv_selectedLocality.setVisibility(View.VISIBLE);
                    selectedLocalityAdapter.setSelectedSource(selectedLocalities);

                }

                tv_locality.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rv_selectedLocality.setVisibility(View.VISIBLE);


                        if (horizontalList.contains(localityList.get(getAdapterPosition()).getLocalityName())) {
                            Toast.makeText(SearchLocality.this, "Already there", Toast.LENGTH_SHORT).show();
                        } else {
                            localityAddedOnHorizontalRecycleView.add(0, localityList.get(getAdapterPosition()).getLocalityName());
                            selectedLocalities.add(0, localityList.get(getAdapterPosition()));
                            selectedLocalityAdapter.setSelectedSource(selectedLocalities);

                            selectedItems.put(getAdapterPosition(), true);
                            notifyDataSetChanged();
                        }

                    }
                });
            }


        }
    }

    class SelectedLocalityAdapter extends RecyclerView.Adapter<SelectedLocalityAdapter.SelectedLocalityHolder> {
        ArrayList<Localities> selectedLocalityList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        SelectedLocalityAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public SelectedLocalityAdapter.SelectedLocalityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.selected_locality_single_item, parent, false);

            return new SelectedLocalityAdapter.SelectedLocalityHolder(view);
        }

        @Override
        public void onBindViewHolder(SelectedLocalityAdapter.SelectedLocalityHolder holder, int position) {


            Localities locality = selectedLocalityList.get(position);

            holder.tv_selectedLocality.setText(locality.getLocalityName());

            //  holder.tv_selectedLocality.setText(locality.getLocalityName());

        }

        @Override
        public int getItemCount() {

            return selectedLocalityList.size();
        }

        public void setSelectedSource(ArrayList<Localities> list) {
            if (list.size() != 0) {
                this.selectedLocalityList = list;

                notifyItemRangeRemoved(0, selectedLocalityList.size());

                notifyDataSetChanged();
            }

        }

        class SelectedLocalityHolder extends RecyclerView.ViewHolder {
            TextView tv_selectedLocality;
            ImageView im_deleteLocality;


            public SelectedLocalityHolder(View itemView) {
                super(itemView);
                tv_selectedLocality = itemView.findViewById(R.id.tv_selectedValue);

                im_deleteLocality = itemView.findViewById(R.id.im_deleteLocality);

                im_deleteLocality.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        localityAddedOnHorizontalRecycleView.remove(selectedLocalityList.get(getAdapterPosition()).getLocalityName());
                        selectedLocalityList.remove(selectedLocalityList.get(getAdapterPosition()));
                        notifyDataSetChanged();
                        adapter.notifyDataSetChanged();

                    }
                });
            }


        }
    }

    //this adapter is for cluster names only..
    class ClusterAdapter extends RecyclerView.Adapter<ClusterAdapter.ClusterHolder> {
        ArrayList<ClusterNames> clusterNamesList = new ArrayList<>();

        LayoutInflater inflater;
        Context context;

        ClusterAdapter(Context context) {
            inflater = LayoutInflater.from(context);

            this.context = context;
        }

        @Override
        public ClusterAdapter.ClusterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.cluster_single_item, parent, false);

            return new ClusterAdapter.ClusterHolder(view);
        }

        @Override
        public void onBindViewHolder(ClusterAdapter.ClusterHolder holder, int position) {


            ClusterNames name = clusterNamesList.get(position);

            holder.tv_clusterName.setText(name.getClusterName());


        }

        @Override
        public int getItemCount() {

            return clusterNamesList.size();
        }

        public void setSelectedSource(ArrayList<ClusterNames> list) {
            if (list.size() != 0) {
                this.clusterNamesList = list;

                notifyItemRangeRemoved(0, clusterNamesList.size());

                notifyDataSetChanged();
            }

        }

        class ClusterHolder extends RecyclerView.ViewHolder {
            TextView tv_clusterName;


            public ClusterHolder(View itemView) {
                super(itemView);
                tv_clusterName = itemView.findViewById(R.id.tv_clusterName);


                tv_clusterName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clusterPosition = getAdapterPosition();
                        clusterName = clusterNamesList.get(getAdapterPosition()).getClusterName();

                        showClusterLocalitiesDialog(clusterName, context);


                    }
                });
            }

            public void showClusterLocalitiesDialog(String clusterName, Context context) {
                final AlertDialog.Builder malert = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.cluster_dialog, null);
                TextView tv_cancel, tv_ok;

                tv_cancel = dialogView.findViewById(R.id.tv_cancel);
                tv_ok = dialogView.findViewById(R.id.tv_ok);

                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                tv_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        //  sendClusterData();


                        dialog.dismiss();

                    }
                });


                rv_clusterLocalityName = dialogView.findViewById(R.id.rv_clusterLocalityNames);

                rv_clusterLocalityName.setLayoutManager(new LinearLayoutManager(context));

                clusterLocalitiesAdapter = new ClusterLocalitiesAdapter(context);
                rv_clusterLocalityName.setAdapter(clusterLocalitiesAdapter);


                getClusterLocalities(Signup_Activity.cityId, clusterName);


                malert.setView(dialogView);

                dialog = malert.create();
                dialog.show();


            }

           /* public void sendClusterData() {
               *//* final JSONArray array = new JSONArray();
                for(int i=0;i<clusterLocalityNames.size();i++)
                {
                    array.put(clusterLocalityNames.get(i));
                }
*//*
                final String sendDataToCluster = "http://www.admin-panel.adecity.com/data/save-group-data";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, Object> params = new HashMap<>();

                        params.put("group_name", clusterName);
                        params.put("name", array);

                        final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, sendDataToCluster, new JSONObject(params),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {


                                        try {
                                            boolean success = response.getBoolean("success");


                                            if (success) {

                                                Toast.makeText(context, "Saved Selected Cluster", Toast.LENGTH_SHORT).show();

                                            } else {

                                                Alert.showAlertDialog(response.getString("msg"), context);

                                            }

                                        } catch (JSONException e) {
//                                            progressDialog.dismiss();
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String errorMessage = vollyErrors.showVollyError(error);
                                //                              progressDialog.dismiss();
                                Alert.showAlertDialog(errorMessage, context);
                                error.printStackTrace();
                            }
                        });

                        queue.add(request_json);


                    }
                }).start();

            }
*/

            public void getClusterLocalities(final String cityId, final String clusterName) {
                final String getClusterLocalityUrl = "http://www.admin-panel.adecity.com/api/get-group-locality";

            /*    progressDialog.setMessage("Getting localities near you");
                progressDialog.show();
*/

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, String> params = new HashMap<String, String>();

                        params.put("city_id", cityId);
                        params.put("group_name", clusterName);

                        final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, getClusterLocalityUrl, new JSONObject(params),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {


                                        try {
                                            boolean success = response.getBoolean("success");


                                            if (success) {
//                                                progressDialog.dismiss();

                                                globalClusterLocalityNamesList = clusterLocalityName(response);


                                                clusterLocalitiesAdapter.setSelectedSource(globalClusterLocalityNamesList);


                                            } else {
                                                //                                              progressDialog.dismiss();

                                                Alert.showAlertDialog(response.getString("msg"), context);
                                                //  btnSignIn.setProgress(100);
                                            }

                                        } catch (JSONException e) {
//                                            progressDialog.dismiss();
                                            e.printStackTrace();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String errorMessage = vollyErrors.showVollyError(error);
                                //                              progressDialog.dismiss();
                                Alert.showAlertDialog(errorMessage, context);
                                error.printStackTrace();
                            }
                        });

                        queue.add(request_json);


                    }
                }).start();


            }

            public ArrayList<ClusterLocalityName> clusterLocalityName(JSONObject response) throws JSONException {
                ArrayList<ClusterLocalityName> clusterLocalityNamesList = new ArrayList<>();
                if (response != null && response.length() != 0) {
                    JSONArray localitiesArray = response.getJSONArray("data");

                    for (int i = 0; i < localitiesArray.length(); i++) {
                        JSONObject localitiesObject = localitiesArray.getJSONObject(i);

                        ClusterLocalityName clusterLocalityName = new ClusterLocalityName();

                        clusterLocalityName.setClusterLocalityName(localitiesObject.getString("name"));

                        clusterLocalityNamesList.add(clusterLocalityName);

                    }


                }

                return clusterLocalityNamesList;


            }

            class ClusterLocalitiesAdapter extends RecyclerView.Adapter<ClusterLocalitiesAdapter.ClusterLocalitiesHolder> {
                ArrayList<ClusterLocalityName> clusterLocalityNamesList = new ArrayList<>();

                LayoutInflater inflater;
                Context context;

                ClusterLocalitiesAdapter(Context context) {
                    inflater = LayoutInflater.from(context);

                    this.context = context;
                }

                @Override
                public ClusterLocalitiesAdapter.ClusterLocalitiesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = inflater.inflate(R.layout.cluster_dialog_single_item, parent, false);

                    return new ClusterLocalitiesAdapter.ClusterLocalitiesHolder(view);
                }

                @Override
                public void onBindViewHolder(ClusterLocalitiesAdapter.ClusterLocalitiesHolder holder, int position) {


                    ClusterLocalityName name = clusterLocalityNamesList.get(position);

                    holder.cb_localityCheckbox.setText(name.getClusterLocalityName());


                }

                @Override
                public int getItemCount() {

                    return clusterLocalityNamesList.size();
                }

                public void setSelectedSource(ArrayList<ClusterLocalityName> list) {
                    if (list.size() != 0) {
                        this.clusterLocalityNamesList = list;

                        notifyItemRangeRemoved(0, clusterLocalityNamesList.size());

                        notifyDataSetChanged();
                    }

                }

                class ClusterLocalitiesHolder extends RecyclerView.ViewHolder {
                    CheckBox cb_localityCheckbox;


                    public ClusterLocalitiesHolder(View itemView) {
                        super(itemView);
                        cb_localityCheckbox = itemView.findViewById(R.id.cb_localityCheckbox);
                        cb_localityCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    clusterLocalityNames[clusterPosition].add(clusterLocalityNamesList.get(getAdapterPosition()).getClusterLocalityName());

                                }
                            }
                        });


                    }


                }

            }


        }
    }
}
