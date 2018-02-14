package firebasecloud.com.firebasecloud;

import android.*;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Active;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Profile;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Tasks;
import firebasecloud.com.firebasecloud.CustomElements.InstantLocation;
import firebasecloud.com.firebasecloud.CustomElements.VollyErrors;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.Volly.vollySingleton;
import firebasecloud.com.firebasecloud.VollyMultiPart.VolleyMultipartRequest;
import firebasecloud.com.firebasecloud.databinding.ActivityMainBinding;
import io.paperdb.Paper;

import static firebasecloud.com.firebasecloud.TakePictureTask.getOutputMediaFile;
import static firebasecloud.com.firebasecloud.TakePictureTask.getResizedBitmap;
import static firebasecloud.com.firebasecloud.TakePictureTask.hasPermissions;
import static firebasecloud.com.firebasecloud.TakePictureTask.imageToString;

public class MainActivity extends AppCompatActivity {
    String[] locationPermissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    int PERMISSION_ALL = 1;
    FloatingActionButton fl_camOpen;
    Dialog imageUploadDialog;

    ImageView imageToUpload;
    ProgressDialog progressDialog;

    Bitmap uploadedBitmap;
    File fileToBeDeleted;
    RequestQueue queue;
    Uri file;
    String userType;

    ActivityMainBinding binding;
    boolean doubleBackToExitPressedOnce = false;

    InstantLocation getUserInstantLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar tabs_scrolling = findViewById(R.id.toolbar);
        setSupportActionBar(tabs_scrolling);

        if(Paper.book().exist("userType"))
        {

            userType=Paper.book().read("userType");
        }
        queue= vollySingleton.getInstance().getRequestQueue();
        progressDialog = new ProgressDialog(this);

        getUserInstantLocation = new InstantLocation(this, this);
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.bn_task: {
                        selectedFragment = Tasks.newInstance("", "");
                        break;
                    }
                    case R.id.bn_active: {
                        selectedFragment = Active.newInstance("", "");
                        break;
                    }
                    case R.id.bn_profile: {
                        selectedFragment = Profile.newInstance("", "");
                        break;
                    }

                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame, selectedFragment);
                transaction.commit();
                return true;


            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, Tasks.newInstance("", ""));
        transaction.commit();
        fl_camOpen = findViewById(R.id.fl_openCam);

        if (userType.equalsIgnoreCase("magazine")) {
            fl_camOpen.setVisibility(View.GONE);
        } else {
            fl_camOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String[] PERMISSIONS = {android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

                    if (!hasPermissions(MainActivity.this, PERMISSIONS)) {

                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);


                    } else {


                        takePicture();


                    }


                    //     ShowUploadDialog(MainActivity.this);
                }
            });

        }


    }

        public void showDialog(Activity activity){
             imageUploadDialog = new Dialog(activity);
            imageUploadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            imageUploadDialog.setCancelable(false);
            imageUploadDialog.setContentView(R.layout.upload_picture_dialog);

            imageUploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView tv_submit = imageUploadDialog.findViewById(R.id.tv_submit);
            TextView tv_cancel = imageUploadDialog.findViewById(R.id.tv_cancel);

            imageToUpload = imageUploadDialog.findViewById(R.id.im_imageToUpload);
            imageToUpload.setImageBitmap(uploadedBitmap);



            tv_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendImageToServer(MainActivity.this);

                }
            });
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageUploadDialog.dismiss();
                }
            });

            imageUploadDialog.show();

        }



    public void ShowUploadDialog(Context context) {
        final AlertDialog.Builder malert = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.upload_picture_dialog, null);

        imageUploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
        TextView tv_submit = dialogView.findViewById(R.id.tv_submit);
        TextView tv_cancel = dialogView.findViewById(R.id.tv_cancel);

        imageToUpload = dialogView.findViewById(R.id.im_imageToUpload);
        imageToUpload.setImageBitmap(uploadedBitmap);



        tv_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendImageToServer(MainActivity.this);

            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUploadDialog.dismiss();
            }
        });


        malert.setView(dialogView);

        imageUploadDialog = malert.create();
        imageUploadDialog.show();

    }

    private void sendImageToServer(final Context context) {

        progressDialog.setMessage("Uploading Details");
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String serverUrl = "http://www.admin-panel.adecity.com/task/save-user-images";

                VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, serverUrl, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                        String resultResponse = new String(response.data);
                        try {
                            JSONObject result = new JSONObject(resultResponse);
                            boolean status = result.getBoolean("success");

                            if (status) {


                                progressDialog.dismiss();
                                final SweetAlertDialog dialog = Alert.showAlertDialog("Picture uploaded successfully", context);

                                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        fileToBeDeleted.delete();
                                        dialog.dismiss();
                                        imageUploadDialog.dismiss();
                                    }
                                });

                            } else {
                                String message = result.getString("msg");

                                progressDialog.dismiss();

                                Alert.showAlertDialog(message, context);
                            }
                        } catch (JSONException e) {
                            Alert.showAlertDialog(e.getMessage(), context);
                            progressDialog.dismiss();
                        }
                    }


                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = VollyErrors.getInstance().showVollyError(error);
                        Alert.showAlertDialog(errorMessage, context);
                        progressDialog.dismiss();
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


                        if (uploadedBitmap != null) {
                            params.put("upload", new DataPart("" + file.getLastPathSegment(), imageToString(uploadedBitmap)));

                        } else {
                            Alert.showAlertDialog("Please choose an Image first", context);
                        }

                        return params;
                    }
                };

                int socketTimeout = 0;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                multipartRequest.setRetryPolicy(policy);

                queue.add(multipartRequest);


            }
        }).start();


    }

    public void takePicture() {
        //Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent camIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        //  camIntent.setClassName("com.android.camera", "com.android.camera.Camera");

        fileToBeDeleted = getOutputMediaFile();
        file = Uri.fromFile(fileToBeDeleted);

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

                uploadedBitmap = getResizedBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), file), 1000);

               // ShowUploadDialog(MainActivity.this);

                showDialog(MainActivity.this);

            } else if (requestCode == 300 && resultCode == RESULT_OK) {


                Toast.makeText(this, "Location service activated", Toast.LENGTH_SHORT).show();


            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onBackPressed() {

        //  int count = getFragmentManager().getBackStackEntryCount();

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInstantLocation.getLocation();
        getUserInstantLocation.changeLocationSetting(PERMISSION_ALL, locationPermissions);
    }


}
