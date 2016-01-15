package com.tunjid.projects.hellomaps.services;

import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tunjid.projects.hellomaps.abstractclasses.CoreApi;
import com.tunjid.projects.hellomaps.customcomponents.Utils;
import com.tunjid.projects.hellomaps.model.response.LocationsResponse;

import rx.Observable;

/**
 * <p> An implementation of a REST client using Retrofit. </p>
 * <p/>
 * <p> Most objects returned in methods are RxJava observables
 * which make asynchronous tasks very easy. They reqire a subscription,
 * which provide a much more robust callback interface.</p>
 * <p/>
 * <p> The implementation of this class is a bound service,
 * therefore only one instance of the API is present throughout the app.</p>
 */
public class HelloApi extends CoreApi
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /**
     * Google API Location.
     */
    private static Location userLastLocation;

    private final IBinder mBinder = new LocalBinder();

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * LocationRequest object
     */

    private LocationRequest locationRequest;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean isInResolution;

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    /**
     * Builds Google API client
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void retryConnecting() {
        isInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Get User location off Google's API.
        userLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(300000); // 5 minutes
        locationRequest.setFastestInterval(45000); // 45 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        startLocationUpdates();
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            // Would have shown error dialog if in an activity
            stopLocationUpdates();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (isInResolution) {
            return;
        }

        // None of the above called, now in resolution
        isInResolution = true;
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (userLastLocation == null) {
            userLastLocation = location;
        }
        else {
            Utils.beans(location, userLastLocation).copy();
        }

        final Intent i = new Intent(Utils.LOCATION_UPDATE);
        i.putExtra(Utils.ACTUAL_LOCATION, userLastLocation);

        sendBroadcast(i);
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
    }

    public static Location getUserLastLocation() {
        return userLastLocation;
    }


    /**
     * <p> Local Binder class that returns Binder interface for clients to bind to. </p>
     */
    public class LocalBinder extends Binder {
        /**
         * <p> gets single unique instance of the API to bind to. </p>
         */
        public HelloApi getService() {
            return HelloApi.this;
        }
    }

    public HelloApi() {
    }

    /**
     * Gets Hello Locations.
     */
    public static Observable<LocationsResponse> getLocations() {
        return apiPointer.getLocations();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
