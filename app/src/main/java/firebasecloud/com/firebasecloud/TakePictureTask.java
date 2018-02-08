package firebasecloud.com.firebasecloud;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.CustomElements.InstantLocation;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.VollyMultiPart.VolleyMultipartRequest;
import firebasecloud.com.firebasecloud.databinding.ActivityTakePictureTaskBinding;

public class TakePictureTask extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    public ActivityTakePictureTaskBinding binding;
    public Uri file;
    public Uri downloadUrl;
    ProgressDialog dialog;
    RequestQueue queue;

    Handler handler;
    String formattedAddress;
    Bitmap userBitmap;
    private ProgressBar pb_showImagesToUpload;
    static File fileToBeDeleted;
    InstantLocation getUserLocation;

    HashMap<String, Double> locationMap;
    String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    int PERMISSION_ALL = 1;
    int totalImagesToUpload, totalImagesRemaining;
    TextView tv_imagesRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_take_picture_task);

        //Instantiate userLocation class
        getUserLocation = new InstantLocation(this, this);

        dialog = new ProgressDialog(this);
        queue = vollySingleton.getInstance().getRequestQueue();
        locationMap = new HashMap<>();
        handler = new Handler();

        tv_imagesRemaining = findViewById(R.id.tv_imageCount);
        pb_showImagesToUpload = findViewById(R.id.pb_showImageCount);

        binding.tvTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (totalImagesRemaining != totalImagesToUpload) {

                    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,

                    };

                    if (!hasPermissions(TakePictureTask.this, PERMISSIONS)) {

                        ActivityCompat.requestPermissions(TakePictureTask.this, PERMISSIONS, PERMISSION_ALL);


                    } else {


                        takePicture();


                    }


                } else {

                    tv_imagesRemaining.setText("Your task has been completed");
                    tv_imagesRemaining.setTextColor(getResources().getColor(R.color.colorUnreadText));
                    tv_imagesRemaining.setTextSize(15);
                    tv_imagesRemaining.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    Alert.showAlertDialog("You have uploaded all the assigned task images  ", TakePictureTask.this);


                }
            }
        });

        binding.tvUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getUserLocation.changeLocationSetting(PERMISSION_ALL,locationPermissions);
                dialog.setMessage("Uploading pictures..");
                dialog.show();

                preparingToSendDetails();


            }
        });



            getRemainingImageCount(TakePictureTask.this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        getUserLocation.getLocation();
        getUserLocation.changeLocationSetting(PERMISSION_ALL,locationPermissions);


        //getLocation(TakePictureTask.this);
        //changeLocationSetting();

    }

    public void getRemainingImageCount(final Context context) {
        final String reSendOtpUrl = "http://www.admin-panel.adecity.com/task/get-user-image-count";


        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();

                params.put("user_id", NewTasks.userId);
                params.put("global_task_id", OnGoing.globalTaskId);


                final JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST, reSendOtpUrl, new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                                try {
                                    boolean success = response.getBoolean("success");


                                    if (success) {

                                        totalImagesRemaining = response.getInt("count");
                                        totalImagesToUpload = response.getInt("totalImagesToUpload");

                                        int totalRemaining = totalImagesToUpload - totalImagesRemaining;
                                        pb_showImagesToUpload.setMax(totalImagesToUpload);
                                        pb_showImagesToUpload.setProgress(totalImagesRemaining);

                                        if (totalImagesRemaining != totalImagesToUpload) {
                                            tv_imagesRemaining.setText("Total images to upload: " + totalRemaining);

                                        }
                                        else {

                                            tv_imagesRemaining.setText("Your task has been completed");
                                            tv_imagesRemaining.setTextColor(getResources().getColor(R.color.colorUnreadText));
                                            tv_imagesRemaining.setTextSize(15);
                                            tv_imagesRemaining.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                            Alert.showAlertDialog("You have uploaded all the assigned task images  ", TakePictureTask.this);


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
                        Alert.showAlertDialog(errorMessage, context);
                        error.printStackTrace();
                    }
                });

                queue.add(request_json);


            }
        }).start();

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public void takePicture() {
        //Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent camIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        //  camIntent.setClassName("com.android.camera", "com.android.camera.Camera");
        file = Uri.fromFile(getOutputMediaFile());

        camIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, file);

        camIntent.putExtra("return-data", true);

        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);


        startActivityForResult(camIntent, 100);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            if (requestCode == 100 && resultCode == RESULT_OK) {


                //   ImageCropFunction();

                userBitmap = getResizedBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), file), 1000);
                binding.imImageUpload.setImageBitmap(userBitmap);

                activateUploadButton();

            } else if (requestCode == 200 && resultCode == RESULT_OK) {

                file = data.getData();
                //         ImageCropFunction();

                userBitmap = getResizedBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()), 1000);
                binding.imImageUpload.setImageBitmap(userBitmap);

                activateUploadButton();
            } else if (requestCode == 300 && resultCode == RESULT_OK) {


                Toast.makeText(this, "Location service activated", Toast.LENGTH_SHORT).show();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);

    }


    public void preparingToSendDetails() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage("Obtaining InstantLocation");

            }
        }, 300);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (InstantLocation.isLocationObtained) {
                    dialog.dismiss();
                    getPlace(Double.parseDouble(InstantLocation.lat), Double.parseDouble(InstantLocation.lon));

                } else {
                    Alert.showAlertDialog("Couldn't able to obtain your InstantLocation,try again", TakePictureTask.this);
                    dialog.dismiss();
                }


            }
        }, 500);


    }

    public String getCurrentTime() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        return timestamp.toString();
    }


    public void sendDetailsToServer(final Context context) {

        dialog.setMessage("Uploading Details");
        dialog.show();
        final String serverUrl = "http://www.admin-panel.adecity.com/task/save-task-data";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, serverUrl, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {

                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    boolean status = result.getBoolean("success");

                    if (status) {


                        dialog.dismiss();
                        final SweetAlertDialog dialog = Alert.showAlertDialog("Picture uploaded successfully", context);

                        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                getRemainingImageCount(context);
                                fileToBeDeleted.delete();
                                dialog.dismiss();
                            }
                        });
                        binding.imImageUpload.setImageResource(R.drawable.green_camera_icon);

                        binding.tvUploadPicture.setEnabled(false);
                        binding.tvUploadPicture.setTextColor(getResources().getColor(R.color.colorSecondaryText));


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

                objectToSend.put("path", String.valueOf(downloadUrl));
                objectToSend.put("time", getCurrentTime());
                objectToSend.put("takenBy", NewTasks.userId);
                objectToSend.put("global_task_id", OnGoing.globalTaskId);
                objectToSend.put("lat", InstantLocation.lat);
                objectToSend.put("lng", InstantLocation.lon);
                objectToSend.put("address", formattedAddress);


                return objectToSend;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();


                /*if (!details_info.isOurGrNumber) {
                    params.put("grSlip", new DataPart("Gr_PhotoCopy.jpg", details_info.imageToString((Bitmap)tempStorage.get("grPhotoCopyBitmap"))));

                }
                params.put("billSlip", new DataPart("Vendor_Bill.jpg", details_info.imageToString((Bitmap)tempStorage.get("vendorBillBitmap"))));
                params.put("weightSlip", new DataPart("WeightSlip.jpg", details_info.imageToString((Bitmap)tempStorage.get("weightSlipBitmap"))));*/

                if (userBitmap != null) {
                    params.put("upload", new DataPart("" + file.getLastPathSegment(), imageToString(userBitmap)));

                } else {
                    Alert.showAlertDialog("Please choose an Image first", TakePictureTask.this);
                }

                return params;
            }
        };

        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        multipartRequest.setRetryPolicy(policy);

        queue.add(multipartRequest);
    }

    public static byte[] imageToString(Bitmap bitmap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }


    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Okhlee monitor");


        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileToBeDeleted = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return fileToBeDeleted;

    }

    public void activateUploadButton() {
        binding.tvUploadPicture.setEnabled(true);
        binding.tvUploadPicture.setTextColor(getResources().getColor(R.color.colorUnreadText));

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_in, R.anim.back_out);

    }


    public boolean runTimePermission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);

            return true;
        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                runTimePermission();
            }

        }

    }




    public void getPlace(final double lat, final double lon) {

        //  System.out.println("Inside getPlace");
        new Thread(new Runnable() {
            @Override
            public void run() {


                JsonObjectRequest jsoon = new JsonObjectRequest(getGeoCodingApi(lat, lon), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            formattedAddress = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");


                            sendDetailsToServer(TakePictureTask.this);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }


                );

                queue.add(jsoon);


            }
        }).start();


    }

    public static String getGeoCodingApi(double lat, double lon) {

        return "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + "" + lon + "&key=" + "AIzaSyCDVqkvSQpLf9NQTfUAHvs9FS-0vp0J7aI";

    }


}




