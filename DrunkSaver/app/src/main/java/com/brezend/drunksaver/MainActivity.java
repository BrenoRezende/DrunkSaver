package com.brezend.drunksaver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int WHATSAPP_REQUEST = 1;
    private static final int MY_PERMISSIONS_REQUEST = 10;
    private GoogleApiClient mGoogleApiClient;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsRequest();
    }

    public void sendMessage(View view) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);

            String gmaps = "";
            if (this.location != null) {
                gmaps = "http://maps.google.com/maps?q=" + this.location.getLatitude() + "," + this.location.getLongitude();
            } else {
                if (!shouldAskPermission()) {
                    Toast.makeText(this, getString(R.string.check_device_gps), Toast.LENGTH_LONG).show();
                }
            }

            String message = getString(R.string.message_to_send) + " - " + getString(R.string.app_name);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, gmaps + "\n *"+message+"*");
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivityForResult(sendIntent, WHATSAPP_REQUEST);

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, getString(R.string.whatsapp_not_installed), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WHATSAPP_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.message_send_success), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.message_send_fail), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void permissionsRequest() {

        if (shouldAskPermission())  {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    openPermissionFragment();
            } else {
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            callConnection();
        }

    }

    public void openPermissionFragment() {
        PermissionFragment permissionFragment = new PermissionFragment();
        permissionFragment.show(getFragmentManager(), "Permission");
    }

    // Permissions request response
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    callConnection();

                } else {
                    Toast.makeText(this, getString(R.string.location_permission_failed), Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    private void callConnection() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    // Check if permission is granted
    private boolean shouldAskPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
