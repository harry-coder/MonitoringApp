package firebasecloud.com.firebasecloud.CustomElements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static firebasecloud.com.firebasecloud.TakePictureTask.hasPermissions;

/**
 * Created by Harpreet on 02/02/2018.
 */

public class InstantLocation implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    public static String lat, lon;
    public static boolean isLocationObtained;
    Activity activity;
    Context context;

    public InstantLocation(Context context, Activity activity) {

        this.context = context;
        this.activity = activity;
    }

    public void getLocation() {


        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        //

    }

    public void changeLocationSetting(final int PERMISSION_ALL, final String... locationPermissions) {
      /*  if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {*/
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS: {
                        if (hasPermissions(context, locationPermissions)) {
                            //noinspection MissingPermission
                            mLastLocation = LocationServices.FusedLocationApi
                                    .getLastLocation(mGoogleApiClient);

                            if (mLastLocation != null) {

                                lat = String.valueOf(mLastLocation.getLatitude());
                                lon = String.valueOf(mLastLocation.getLongitude());


                                isLocationObtained = true;

                            } else {
                                // TodayRate.showAlertDialog("Couldn't able to fetch your InstantLocation. Please restart it", LoginActivity.this);
                                // changeLocationSetting();
                                isLocationObtained = false;
                            }
                        } else {
                            ActivityCompat.requestPermissions(activity, locationPermissions, PERMISSION_ALL);

                        }
                        break;
                    }

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, 300);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
