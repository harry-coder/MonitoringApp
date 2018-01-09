package firebasecloud.com.firebasecloud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnBackPressListener;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.VollyMultiPart.VolleyMultipartRequest;
import firebasecloud.com.firebasecloud.databinding.ActivityTakePictureTaskBinding;

public class TakePictureTask extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public ActivityTakePictureTaskBinding binding;
    public Uri file;
    public Uri downloadUrl;
    ProgressDialog dialog;
    RequestQueue queue;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    boolean isLocationObtained;
    Handler handler;
    String lat, lon;
    JSONObject objectToSend;
    Bitmap userBitmap;

    HashMap<String, Double> locationMap;
    String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    int PERMISSION_ALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_take_picture_task);


       dialog = new ProgressDialog(this);
        queue = vollySingleton.getInstance().getRequestQueue();
        locationMap = new HashMap<>();
        handler = new Handler();

        binding.tvTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,

                };

                if (!hasPermissions(TakePictureTask.this,PERMISSIONS)) {

                    ActivityCompat.requestPermissions(TakePictureTask.this, PERMISSIONS, PERMISSION_ALL);
               /*     showDialog();
                    binding.tvUploadPicture.setEnabled(true);
                    binding.tvUploadPicture.setTextColor(getResources().getColor(R.color.colorUnreadText));

                    binding.tvInstructions.setText("Now click on upload picture to upload it");*/

        //            runTimePermission();
             //       ActivityCompat.requestPermissions(TakePictureTask.this, PERMISSIONS, PERMISSION_ALL);
          //          System.out.println("in permission");

                } else {

                    showDialog();


                        // runTimePermission();

                    }


            }
        });

        binding.tvUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkLocationService()) {
                    // dialog.dismiss();
                    dialog.setMessage("Uploading pictures..");
                    dialog.show();
                    getLocation(TakePictureTask.this);

                    preparingToSendDetails();
                }

            }
        });

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


    private void showDialog() {
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(new ViewHolder(R.layout.dialog)).setGravity(Gravity.TOP)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {


                        if (view.getId() == R.id.Camera) {


                            takePicture();

                            dialog.dismiss();

                        } else if (view.getId() == R.id.Gallery) {


                            takePictureByGallery();
                            dialog.dismiss();

                        }

                    }


                }).setCancelable(false).setOnBackPressListener(new OnBackPressListener() {
                    @Override
                    public void onBackPressed(DialogPlus dialogPlus) {


                        dialogPlus.dismiss();


                    }
                }).setCancelable(true).setGravity(Gravity.CENTER).setContentBackgroundResource(Color.TRANSPARENT)
                // This will enable the expand feature, (similar to android L share dialog)
                .create();
        dialog.show();


    }

    public void takePictureByGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

        startActivityForResult(intent, 200);

    }

    public void takePicture() {
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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



                ImageCropFunction();

           /*     userBitmap = getResizedBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), file), 1000);
                binding.imImageUpload.setImageBitmap(userBitmap);

                activateUploadButton();
*/

            } else if (requestCode == 200 && resultCode == RESULT_OK) {

                file = data.getData();
                ImageCropFunction();

  /*              userBitmap = getResizedBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()), 1000);
                binding.imImageUpload.setImageBitmap(userBitmap);

                activateUploadButton();
*/
            }
            else if (requestCode == 1) {

                if (data != null) {

                    Bundle bundle = data.getExtras();

                //    Bitmap bitmap = null;
                    if (bundle != null) {
                        userBitmap = bundle.getParcelable("data");
                    }

                    binding.imImageUpload.setImageBitmap(userBitmap);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public void ImageCropFunction() {

        // Image Crop Code
        try {
            Intent   CropIntent = new Intent("com.android.camera.action.CROP");

            CropIntent.setDataAndType(file, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 180);
            CropIntent.putExtra("outputY", 180);
            CropIntent.putExtra("aspectX", 3);
            CropIntent.putExtra("aspectY", 4);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, 1);

            activateUploadButton();
        } catch (ActivityNotFoundException e) {

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
                dialog.setMessage("Obtaining location");

            }
        },1000);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLocationObtained) {
                    dialog.dismiss();
                    sendDetailsToServer(TakePictureTask.this);

                } else {
                    Alert.showAlertDialog("Couldn't able to obtain your location,try again", TakePictureTask.this);
                    dialog.dismiss();
                }


            }
        },3000);


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
                boolean    status = result.getBoolean("success");

                    if (status) {


                        dialog.dismiss();
                        Alert.showAlertDialog("Picture uploaded successfully",context);

                       binding.imImageUpload.setImageResource(R.drawable.green_camera_icon);

                       binding.tvUploadPicture.setEnabled(false);
                        binding.tvUploadPicture.setTextColor(getResources().getColor(R.color.colorSecondaryText));


                    } else {
                       String   message = result.getString("msg");

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
                objectToSend.put("takenBy", LoginActivity.userId);
                objectToSend.put("task_id", OnGoing.taskId);
                objectToSend.put("lat", lat);
                objectToSend.put("lng", lon);


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

                if (userBitmap!=null) {
                    params.put("upload", new DataPart(""+file.getLastPathSegment(), imageToString(userBitmap)));

                }
                else
                {
                    Alert.showAlertDialog("Please choose an Image first",TakePictureTask.this);
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
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    public void activateUploadButton()
    {
        binding.tvUploadPicture.setEnabled(true);
        binding.tvUploadPicture.setTextColor(getResources().getColor(R.color.colorUnreadText));

    }






    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_in, R.anim.back_out);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());


        /*if (checkLocationService()) {
        */
        if (hasPermissions(TakePictureTask.this,locationPermissions)) {
            //noinspection MissingPermission
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            //   System.out.println(  mLastLocation.getLatitude());

            if (mLastLocation != null) {

                lat = String.valueOf(mLastLocation.getLatitude());
                lon = String.valueOf(mLastLocation.getLongitude());


                isLocationObtained = true;
            } else {
                // TodayRate.showAlertDialog("Couldn't able to fetch your location. Please restart it", LoginActivity.this);
                isLocationObtained = false;
            }
        }
        else
        {
            ActivityCompat.requestPermissions(TakePictureTask.this, locationPermissions, PERMISSION_ALL);

        }

    }

    /*  }
  */
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Alert.showAlertDialog(connectionResult.getErrorMessage(), this);

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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED&& grantResults[2] == PackageManager.PERMISSION_GRANTED&& grantResults[3] == PackageManager.PERMISSION_GRANTED&& grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                runTimePermission();
            }

        }

    }



    public void getLocation(final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient = new GoogleApiClient.Builder(context)
                        .addConnectionCallbacks(TakePictureTask.this)
                        .addOnConnectionFailedListener(TakePictureTask.this)
                        .addApi(LocationServices.API).build();

                mGoogleApiClient.connect();



            }
        }).start();

    }

    public boolean checkLocationService() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            dialog.dismiss();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(R.string.enable_location);
            dialog.setPositiveButton(R.string.enable_it, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);

                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    checkLocationService();

                    paramDialogInterface.dismiss();

                }
            });
            dialog.show();
        } else {
            return true;
        }
        return false;


    }



}
