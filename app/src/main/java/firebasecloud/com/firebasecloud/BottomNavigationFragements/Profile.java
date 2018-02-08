package firebasecloud.com.firebasecloud.BottomNavigationFragements;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.Alert;
import firebasecloud.com.firebasecloud.IntroActivity;
import firebasecloud.com.firebasecloud.LoginActivity;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.TakePictureTask;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.VollyMultiPart.VolleyMultipartRequest;
import io.paperdb.Paper;


public class Profile extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ImageView im_userDetails, im_pieDetails;
    ViewFlipper flipper;
    RequestQueue queue;
    PieChart pieChart;
    ProgressDialog dialog;
    ArrayList<Integer> pieChartDataList;
    TextView tv_userName, tv_userCity, tv_userMobile, tv_userType, tv_complete, tv_snoozed, tv_ended, tv_name, tv_taskPercent;

    Switch st_userAvailability;

    Bitmap userImage;
    HashMap<String, Integer> valueMap;
    String userChoosenTask;
    ImageView im_userImage,im_logout;
    int completeCount, endedCount, pendingCount;
    String status;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
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
        queue = vollySingleton.getInstance().getRequestQueue();
        pieChartDataList = new ArrayList<>();
        dialog = new ProgressDialog(getActivity());
        valueMap = new HashMap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tv_userName = view.findViewById(R.id.tv_userName);
        tv_userCity = view.findViewById(R.id.tv_city);
        tv_userType = view.findViewById(R.id.tv_userType);
        tv_userMobile = view.findViewById(R.id.tv_mobile);


        tv_name = view.findViewById(R.id.tv_name);
        im_userDetails = view.findViewById(R.id.im_userDetails);
        im_pieDetails = view.findViewById(R.id.im_chartDetails);
        im_logout=view.findViewById(R.id.im_logout);

        flipper = view.findViewById(R.id.flipper);

        tv_taskPercent = view.findViewById(R.id.tv_taskPercent);
        im_userImage = view.findViewById(R.id.im_userImage);

        tv_complete = view.findViewById(R.id.tv_complete);
        tv_snoozed = view.findViewById(R.id.tv_snoozed);
        tv_ended = view.findViewById(R.id.tv_ended);
        st_userAvailability = view.findViewById(R.id.st_changeStatus);

        st_userAvailability.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    System.out.println("Inside true");
                    changeUserAvailabilityStatus("Available", getActivity());

                } else {
                    System.out.println("Inside false");
                    changeUserAvailabilityStatus("Unavailable", getActivity());

                }
            }
        });

        getPieChartDetails(getActivity());


        getUserDetails(getActivity());



     /*   tv_complete.setText(pieChartDataList.get(0));
        tv_snoozed.setText(pieChartDataList.get(1));
        tv_ended.setText(pieChartDataList.get(2));
*/
        im_userDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right); // load an animation
                flipper.setOutAnimation(out); // set out Animation for ViewSwitcher
                im_userDetails.setVisibility(View.INVISIBLE);

                im_pieDetails.setVisibility(View.VISIBLE);
                flipper.showNext();

            }
        });
        im_pieDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                Animation out = AnimationUtils.loadAnimation(getActivity(),android.R.anim.slide_in_left); // load an animation
                flipper.setOutAnimation(out); // set out Animation for ViewSwitcher

*/
                im_userDetails.setVisibility(View.VISIBLE);

                im_pieDetails.setVisibility(View.INVISIBLE);
                flipper.showNext();


            }
        });


        pieChart = view.findViewById(R.id.piechart);

        im_userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectImage(getActivity());

            }
        });

        if (Paper.book().exist("imageUrl")) {
            String url = Paper.book().read("imageUrl");
            setImageUsingPicasso(getActivity(), url);
        }

        im_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        return view;
    }

    private void changeUserAvailabilityStatus(final String status, final Context context) {


        final String userDetailUrl = "http://www.admin-panel.adecity.com/api/change-status";


        new Handler().post(new Runnable() {
            @Override
            public void run() {

                HashMap<String, Object> params = new HashMap<>();

                params.put("user_id", NewTasks.userId);
                params.put("status", status);


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, userDetailUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {

                                        if (status.equalsIgnoreCase("available")) {
                                            Alert.showAlertDialog("Your status changes to available", context);
                                        } else {
                                            Alert.showAlertDialog("Your status changes to unavailable", context);

                                        }

                                    }

                                } catch (JSONException e) {
                                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
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
                        }
                        error.printStackTrace();
                        Alert.showAlertDialog(errorMessage, context);
                    }
                });

                queue.add(request_json);


            }
        });

    }

    private void selectImage(final Context context) {
        final String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        final CharSequence[] items = {"Take Photo", "Choose from Gallery",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Image!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = TakePictureTask.hasPermissions(context, PERMISSIONS);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 10);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), 20);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 20)
                onSelectFromGalleryResult(data);
            else if (requestCode == 10)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        userImage = null;
        if (data != null) {
            try {

                userImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sendUserImage(getActivity());
        //    transformedBitmap=getRoundedCroppedBitmap(userImage);
        //  im_userImage.setImageBitmap(transformedBitmap);

        //  Paper.book().write("userImage",transformedBitmap);

    }


    private void onCaptureImageResult(Intent data) {
        userImage = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (userImage != null) {
            userImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        }
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;

        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendUserImage(getActivity());

        // transformedBitmap=getRoundedCroppedBitmap(userImage);

        //im_userImage.setImageBitmap(transformedBitmap);

        //Paper.book().write("userImage",transformedBitmap);
    }


    public void sendUserImage(final Context context) {
        dialog.setMessage("Uploading Image");
        dialog.show();
        final String serverUrl = "http://www.admin-panel.adecity.com/api/upload-profile-image";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, serverUrl, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {

                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    boolean status = result.getBoolean("success");

                    if (status) {


                        dialog.dismiss();

                        String url = result.getString("url");
                        Paper.book().write("imageUrl", url);
                        setImageUsingPicasso(context, url);
                    } else {
                        String message = result.getString("msg");

                        dialog.dismiss();

                        Alert.showAlertDialog(message, context);
                    }
                } catch (JSONException e) {
                    Alert.showAlertDialog(e.getMessage(), context);
                    dialog.dismiss();
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
                    dialog.dismiss();
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
                    dialog.dismiss();
                }
                Alert.showAlertDialog(errorMessage, context);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> objectToSend = new HashMap<>();

                objectToSend.put("user_id", NewTasks.userId);


                return objectToSend;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();


                if (userImage != null) {
                    params.put("upload", new DataPart("Image", TakePictureTask.imageToString(userImage)));

                }

                return params;
            }
        };

        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        multipartRequest.setRetryPolicy(policy);

        queue.add(multipartRequest);
    }


    public void getUserDetails(final Context context) {

        final String userDetailUrl = "http://www.admin-panel.adecity.com/api/profile";


        new Handler().post(new Runnable() {
            @Override
            public void run() {

                HashMap<String, Object> params = new HashMap<>();

                params.put("user_id", NewTasks.userId);


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, userDetailUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");

                                    System.out.println("user " + response);

                                    if (success) {

                                        if (response.length() != 0) {
                                            JSONArray userDetailArray = response.getJSONArray("result");
                                            JSONObject userObject = userDetailArray.getJSONObject(0);
                                            tv_userName.setText(userObject.getString("name"));
                                            tv_name.setText(userObject.getString("name"));
                                            tv_userCity.setText(userObject.getString("user_city"));
                                            tv_userType.setText(userObject.getString("user_type"));
                                            tv_userMobile.setText(userObject.getString("mobile"));
                                            status = userObject.getString("status");

                                            if (status != null) {
                                                if (status.equalsIgnoreCase("available")) {
                                                    st_userAvailability.setChecked(true);
                                                } else {
                                                    st_userAvailability.setChecked(false);
                                                }
                                            }


                                        }

                                    } else {


                                    }

                                } catch (JSONException e) {
                                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
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
                        }
                        error.printStackTrace();
                        Alert.showAlertDialog(errorMessage, context);
                    }
                });

                queue.add(request_json);


            }
        });
    }

    public void getPieChartDetails(final Context context) {
        final String pieChartDetailUrl = "http://www.admin-panel.adecity.com/task/get-task-status-count";


        new Handler().post(new Runnable() {
            @Override
            public void run() {

                HashMap<String, Object> params = new HashMap<>();

                params.put("user_id", NewTasks.userId);


                JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, pieChartDetailUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {

                                        if (response.length() != 0) {
                                            if (isAdded())
                                                retrievePieChartData(response);
                                        }

                                    } else {


                                    }

                                } catch (JSONException e) {
                                    Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
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
                        }
                        error.printStackTrace();
                        Alert.showAlertDialog(errorMessage, context);

                    }
                });

                queue.add(request_json);


            }
        });
    }

    private void retrievePieChartData(JSONObject response) throws JSONException {

        JSONArray pieChartArray = response.getJSONArray("result");

        for (int i = 0; i < pieChartArray.length(); i++) {
            JSONObject pieChartObject = pieChartArray.getJSONObject(i);

            //    System.out.println(pieChartObject);

            valueMap.put(pieChartObject.getString("_id"), pieChartObject.getInt("count"));

            //    pieChartDataList.add(pieChartObject.getInt("count"));
        }

        int colors[] = {getResources().getColor(R.color.expiredColor), getResources().getColor(R.color.rejectedColor), getResources().getColor(R.color.colorUnreadText)};

        List<Entry> yvalues = new ArrayList<Entry>();

        if (valueMap.containsKey("Complete")) {
            completeCount = valueMap.get("Complete");

        }
        if (valueMap.containsKey("Ended")) {
            endedCount = valueMap.get("Ended");
        }
        if (valueMap.containsKey("Pending")) {
            pendingCount = valueMap.get("Pending");
        }
        //  for(int i=0;i<pieChartDataList.size()-1;i++) {

        yvalues.add(new Entry(pendingCount, 0));
        yvalues.add(new Entry(endedCount, 1));
        yvalues.add(new Entry(completeCount, 2));

            /*   yvalues.add(new Entry(pieChartDataList.get(1), 1));
            yvalues.add(new Entry(pieChartDataList.get(2), 2));*/
        //}
        /*tv_complete.setText(pieChartDataList.get(0));
        tv_snoozed.setText(pieChartDataList.get(1));
        tv_ended.setText(pieChartDataList.get(2));
*/
        PieDataSet dataSet = new PieDataSet(yvalues, "");
        //   dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        dataSet.setColors(colors);

        ArrayList<String> xVals = new ArrayList<String>();

        xVals.add("Complete");
        xVals.add("Snoozed");
        xVals.add("Ended");

        PieData data = new PieData(xVals, dataSet);

        pieChart.setData(data);
        pieChart.animateY(1000);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDescription("");
        pieChart.setHoleRadius(80);
        pieChart.setDrawSliceText(false);


        pieChart.invalidate();


        getTaskCount();

    }

    public void getTaskCount() {
        // if (pieChartDataList.get(0).toString() != null)
        tv_snoozed.setText(String.valueOf(pendingCount));

        //else if (pieChartDataList.get(1).toString() != null)
        tv_ended.setText(String.valueOf(endedCount));
        //else if (pieChartDataList.get(2).toString() != null)

        //{
        tv_complete.setText(String.valueOf(completeCount));


        tv_taskPercent.setText("Good you have completed " + completeCount + " task this month");
        //}
    }

    public void setImageUsingPicasso(Context context, String url) {
        Picasso.with(context).load(url)
                .resize(120, 120)
                .into(im_userImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap imageBitmap = ((BitmapDrawable) im_userImage.getDrawable()).getBitmap();
                        RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                        imageDrawable.setCircular(true);
                        imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                        im_userImage.setImageDrawable(imageDrawable);
                    }

                    @Override
                    public void onError() {
                        im_userImage.setImageResource(R.drawable.okhlee_logo);
                    }
                });
    }


    public void logoutUser() {


        Paper.book().write("isUserLoggedOut", true);
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.back_in, R.anim.back_out);


    }
}
