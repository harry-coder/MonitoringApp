package firebasecloud.com.firebasecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
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
import firebasecloud.com.firebasecloud.CustomElements.CompleteTaskPojo;
import firebasecloud.com.firebasecloud.CustomElements.Localities;
import firebasecloud.com.firebasecloud.CustomElements.VollyErrors;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import io.paperdb.Paper;

import static firebasecloud.com.firebasecloud.TaskFragments.NewTasks.getDate;

public class SearchLocality extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ProgressDialog progressDialog;
    VollyErrors vollyErrors;
    RequestQueue queue;
    RecyclerView localityRecycleView;
    SearchView localitySearch;
    ArrayList<Localities> globalLocalityArrayList;
    LocalityAdapter adapter;
    RecyclerView rv_selectedLocality;
    SelectedLocalityAdapter selectedLocalityAdapter;
    public static ArrayList<Localities> selectedLocalities;
    private SparseBooleanArray selectedItems;
    TextView tv_cancel, tv_submit;
    ArrayList <String>horizontalList;


    ArrayList<String> localityAddedOnHorizontalRecycleView;

    String selectedLocality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_locality);

        localitySearch = findViewById(R.id.sv_searchView);
        localitySearch.setQueryHint("Select Locality for city ");

        tv_cancel = findViewById(R.id.tv_cancel);
        tv_submit = findViewById(R.id.tv_submit);

        horizontalList = new ArrayList<String>();

        localitySearch.setBackgroundColor(Color.parseColor("#e927a4d1"));
        localitySearch.setOnQueryTextListener(this);
        queue = vollySingleton.getInstance().getRequestQueue();
        progressDialog = new ProgressDialog(this);
        globalLocalityArrayList = new ArrayList<>();

        //datasource for horizontal view;
        if (Paper.book().exist(Signup_Activity.selectedCity)) {

            selectedLocalities = new ArrayList<>();
            selectedLocalities = Paper.book().read(Signup_Activity.selectedCity);
            horizontalList=Paper.book().read(Signup_Activity.selectedCity);
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

        if(Paper.book().exist("selectedLocality"))
        {

            horizontalList=Paper.book().read("selectedLocality");

        }
        else {
            localityAddedOnHorizontalRecycleView=new ArrayList<>();
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
                                        adapter.setSource(globalLocalityArrayList);

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


                        if(horizontalList.contains(localityList.get(getAdapterPosition()).getLocalityName()))
                        {
                            Toast.makeText(SearchLocality.this, "Alread there", Toast.LENGTH_SHORT).show();
                        }
                        else {
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

            holder.tv_selectedLocality.setText(locality.getLocalityName());

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

}
