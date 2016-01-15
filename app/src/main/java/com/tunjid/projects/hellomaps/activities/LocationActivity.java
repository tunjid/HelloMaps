package com.tunjid.projects.hellomaps.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tunjid.projects.hellomaps.R;
import com.tunjid.projects.hellomaps.abstractclasses.CoreActivity;
import com.tunjid.projects.hellomaps.customcomponents.FloatingActionButton;
import com.tunjid.projects.hellomaps.customcomponents.Utils;
import com.tunjid.projects.hellomaps.fragments.LocationDetailFragment;
import com.tunjid.projects.hellomaps.fragments.LocationFragment;
import com.tunjid.projects.hellomaps.model.pojo.Location;
import com.tunjid.projects.hellomaps.services.HelloApi;

public class LocationActivity extends CoreActivity {

    public static final String LOCATIONS_TAG = "LOCATIONS_TAG";
    public static final String LOCATIONS_DETAIL_TAG = "LOCATIONS_DETAIL_TAG";
    public static final String CAN_REQUEST_LOCATION = "CAN_REQUEST_LOCATION";

    private boolean canRequestLocation = true;
    private ProgressDialog locationProgressDialog;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        api = ((HelloApi.LocalBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    private final BroadcastReceiver broadcastUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case Utils.LOCATION_UPDATE:

                    if (locationProgressDialog != null && locationProgressDialog.isShowing()) {
                        locationProgressDialog.dismiss();
                        Toast.makeText(LocationActivity.this, R.string.location_updated, Toast.LENGTH_SHORT).show();
                    }

                    LocationFragment locationFragment =
                            (LocationFragment) getSupportFragmentManager().findFragmentByTag(LOCATIONS_TAG);

                    if (locationFragment != null) {
                        locationFragment.refreshIfLoaded();
                    }
                    break;
            }
        }
    };

    private static IntentFilter apiBroadcastUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.LOCATION_UPDATE);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Bind to the API service

        Intent APIintent = new Intent(this, HelloApi.class);
        bindService(APIintent, this, BIND_AUTO_CREATE);

        initializeViewComponents();

        if (isSavedInstance) {
            canRequestLocation = savedInstanceState.getBoolean(CAN_REQUEST_LOCATION);
            currentFragment = savedInstanceState.getString(Utils.CURRENT_FRAGMENT);
            assert currentFragment != null;

            switch (currentFragment) {
                case LOCATIONS_TAG:
                    LocationFragment locationFragment = (LocationFragment)
                            getSupportFragmentManager().getFragment(savedInstanceState, LOCATIONS_TAG);

                    slideUpTransaction()
                            .replace(R.id.container, locationFragment, LOCATIONS_TAG)
                            .commit();
                    break;
                case LOCATIONS_DETAIL_TAG:
                    LocationDetailFragment locationDetailFragment = (LocationDetailFragment)
                            getSupportFragmentManager().getFragment(savedInstanceState, LOCATIONS_DETAIL_TAG);

                    slideUpTransaction()
                            .replace(R.id.container, locationDetailFragment, LOCATIONS_DETAIL_TAG)
                            .commit();
                    break;
            }
        }
        else {
            slideUpTransaction()
                    .addToBackStack(LOCATIONS_TAG)
                    .replace(R.id.container, LocationFragment.newInstance(LOCATIONS_TAG), LOCATIONS_TAG)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(CAN_REQUEST_LOCATION, canRequestLocation);
        outState.putString(Utils.CURRENT_FRAGMENT, currentFragment);


        FragmentManager fm = getSupportFragmentManager();

        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {

            // Zero indexed
            String backStackEntry = fm.getBackStackEntryAt(i).getName();

            switch (backStackEntry) {
                case LOCATIONS_TAG:
                    LocationFragment locationFragment
                            = (LocationFragment) fm.findFragmentByTag(LOCATIONS_TAG);
                    getSupportFragmentManager().putFragment(outState, LOCATIONS_TAG, locationFragment);
                    break;
                case LOCATIONS_DETAIL_TAG:
                    LocationDetailFragment locationDetailFragment
                            = (LocationDetailFragment) fm.findFragmentByTag(LOCATIONS_DETAIL_TAG);
                    getSupportFragmentManager().putFragment(outState, LOCATIONS_DETAIL_TAG, locationDetailFragment);
                    break;
            }
        }


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastUpdateReceiver, apiBroadcastUpdateIntentFilter());

        // Check if the location has been changed recently.
        // If it has, update location to new location and force refresh
        if (locationProgressDialog != null && locationProgressDialog.isShowing()) {
            if (!locationDisabled() && api != null) {
                api.startLocationUpdates();
            }
            else {
                canRequestLocation = false;
                locationProgressDialog.dismiss();
                Toast.makeText(this, R.string.location_failed, Toast.LENGTH_SHORT).show();
            }
        }

        if (locationDisabled() && canRequestLocation) {
            requestLocation();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastUpdateReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 1) {

            // Subtract 2; zero indexed, and I want what's behind
            String backStackEntry = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2).getName();

            switch (backStackEntry) {

                case LOCATIONS_TAG:
                    toolbar.setTitle(getString(R.string.all_locations));
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
                    break;
                case LOCATIONS_DETAIL_TAG:
                    toolbar.setTitle(getString(R.string.map));
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
                    break;
            }
            currentFragment = backStackEntry;
            fm.popBackStack();
            restoreActionBar();
        }
        else {
            finish();
        }
    }

    @Override
    public void afterActivityCreated(Bundle fragmentArgs) {
        super.afterActivityCreated(fragmentArgs);
        switch (currentFragment) {
            case LOCATIONS_TAG:
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
                toolbarTitle = getString(R.string.all_locations);
                break;
            case LOCATIONS_DETAIL_TAG:
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
                toolbarTitle = getString(R.string.map);
                break;
        }
        restoreActionBar();
    }

    public void onLocationClicked(Location location) {

        defaultTransaction()
                .addToBackStack(LOCATIONS_DETAIL_TAG)
                .replace(R.id.container, LocationDetailFragment.newInstance(LOCATIONS_DETAIL_TAG, location), LOCATIONS_DETAIL_TAG)
                .commit();
    }

    public boolean isNetworkAvailable() {
        final ConnectivityManager connectivityManager =
                ((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE));

        return connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    /**
     * Initializes view components.
     */
    public void initializeViewComponents() {

        toolbar = (Toolbar) findViewById(R.id.action_bar); // Find Toolbar used as ActionBar
        fab = (FloatingActionButton) findViewById(R.id.fab);

        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mainContent = (RelativeLayout) findViewById(R.id.container);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        fab.hide();
    }

    public boolean locationDisabled() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return !gpsEnabled && !networkEnabled;
    }

    /**
     * Requests the user's location if it's null and needed
     */
    public void requestLocation() {

        if (locationDisabled()) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);

            dialog.setMessage(getString(R.string.request_location));

            dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    onLocationDenied();
                    dialog.dismiss();
                }
            });

            dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    locationProgressDialog = ProgressDialog.show(LocationActivity.this, "", getString(R.string.getting_location), true);
                    locationProgressDialog.setCanceledOnTouchOutside(true);
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                    dialog.dismiss();
                }
            });

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    onLocationDenied();
                }
            });

            dialog.show();
        }
    }

    public void onLocationDenied() {

    }


}
