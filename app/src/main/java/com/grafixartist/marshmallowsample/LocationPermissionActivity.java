package com.grafixartist.marshmallowsample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

public class LocationPermissionActivity extends AppCompatActivity implements OnPermissionCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;

    PermissionHelper permissionHelper;
    Toolbar toolbar;
    TextView status;
    LocationRequest mLocationRequest;
    final String DIALOG_TITLE = "Access Location";
    final String DIALOG_MESSAGE = "We need to access your location to get the data.";
    String mLastUpdateTime;

    protected static final String TAG = "MainActivity";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    Button button;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected boolean mRequestingLocationUpdates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_permission);
        button = (Button) findViewById(R.id.button);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        status = (TextView) findViewById(R.id.status);
        permissionHelper = PermissionHelper.getInstance(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                permissionHelper.setForceAccepting(false).request(PERMISSION);
           
            }
        });

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
        status.setText("On created");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        status.setText("onRequestPermissionsResult() " + Arrays.toString(permissions)
                + "\nRequest Code: " + requestCode
                + "\nGrand Results: " + grantResults);

        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        status.setText("onActivityResult()");

        permissionHelper.onActivityForResult(requestCode);
    }

    @Override
    public void onPermissionGranted(String[] permissionName) {

        status.setText("onPermissionGranted() " + Arrays.toString(permissionName));
        getLocation();
        status.setText(mLatitudeLabel + " " + mLongitudeLabel);

    }

    @Override
    public void onPermissionDeclined(String[] permissionName) {
        status.setText("onPermissionDeclined() " + Arrays.toString(permissionName));


    }

    @Override
    public void onPermissionPreGranted(String permissionsName) {
        status.setText("onPermissionPreGranted() " + permissionsName);
        getLocation();
        status.setText(mLatitudeLabel + " " + mLongitudeLabel);
    }


    /**
     * Called when the user denied permission 1st time in System dialog
     *
     * @param permissionName
     */
    @Override
    public void onPermissionNeedExplanation(String permissionName) {
        status.setText("onPermissionNeedExplanation() " + permissionName);

        /*
        Show dialog here and ask permission again. Say why
         */

        showAlertDialog(DIALOG_TITLE, DIALOG_MESSAGE, PERMISSION);

    }

    @Override
    public void onPermissionReallyDeclined(String permissionName) {
        status.setText("onPermissionReallyDeclined() " + permissionName + "\nCan only be granted from settingsScreen");
//        Log.i("ReallyDeclined", "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        status.setText("onNoPermissionNeeded() fallback for pre Marshmallow ");
        status.setText(mLatitudeLabel + " " + mLongitudeLabel);
    }


    private void showAlertDialog(String title, String message, final String permission) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        permissionHelper.requestAfterExplanation(permission);

                    }
                })
                .create();

        dialog.show();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(LocationPermissionActivity.this, "No permission", Toast.LENGTH_SHORT).show();
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
              mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
              updateUI();
          }

          // If the user presses the Start Updates button before GoogleApiClient connects, we set
          // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
          // the value of mRequestingLocationUpdates and if it is true, we start location updates.
          if (mRequestingLocationUpdates) {
              startLocationUpdates();
          }


    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        status.setText("Status " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        status.setText("Status " + connectionResult.getErrorMessage()+ " " + connectionResult.getErrorCode() + " " + connectionResult.getResolution().toString());
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        if(!mGoogleApiClient.isConnected())
        {
            if(mGoogleApiClient.isConnecting()){
                Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_LONG).show();
                return;
            }
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeLabel =  String.valueOf(mLastLocation.getLatitude());
            mLongitudeLabel = String.valueOf(mLastLocation.getLongitude());
        }

    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates state = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        //request location
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        showAlertDialog(DIALOG_TITLE, DIALOG_MESSAGE, PERMISSION);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Toast.makeText(getApplicationContext(), "Settings unavailable",Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "LocationPermission Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.grafixartist.marshmallowsample/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "LocationPermission Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.grafixartist.marshmallowsample/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        if(mCurrentLocation != null)
        status.setText("LAt-  " +String.valueOf(mCurrentLocation.getLatitude()) + " Long- " + String.valueOf(mCurrentLocation.getLongitude()) + " time- "+ mLastUpdateTime);
    }
}
