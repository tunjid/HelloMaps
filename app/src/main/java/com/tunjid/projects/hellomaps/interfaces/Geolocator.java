package com.tunjid.projects.hellomaps.interfaces;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Class that handles geolocation
 */
@SuppressWarnings("unused")
public class Geolocator implements Observer<String> {

    private final static String GEOLOCATION_FAILURE = "GEOLOCATION_FAILURE";

    GeolocationListener geolocationListener;

    public Geolocator(GeolocationListener geolocationListener) {
        this.geolocationListener = geolocationListener;
    }

    public void geolocate(final Location googleLocation) {

        Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                return Observable.just(geolocateActual(googleLocation));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(12, TimeUnit.SECONDS)
                .subscribe(this);
    }

    // Use Geocoder class to parse location gotten from Google's Location API.

    public String geolocateActual(final Location googleLocation) {

        String result = GEOLOCATION_FAILURE;

        try {
            if (googleLocation != null) {

               final Geocoder gcd = new Geocoder(geolocationListener.getContext().getApplicationContext(), Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(
                        googleLocation.getLatitude(),
                        googleLocation.getLongitude(), 1);

                if (addresses.size() > 0) { // If a result was found pass the location to the next activity.

                    final Address address = addresses.get(0);

                    // In the United states
                    if (address.getAdminArea() != null && address.getLocality() != null) {

                        String locality = address.getLocality();
                        String adminArea = address.getAdminArea(); // abbreviate state name

                        result = locality + ", " + adminArea;
                    }
                    // International
                    else if (address.getAdminArea() != null) {
                        result = address.getAdminArea();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void onDestroy() {
        geolocationListener = null;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(String locationToSearchFor) {
        switch (locationToSearchFor) {
            case GEOLOCATION_FAILURE:
                geolocationListener.onGeolocationFaliure();
                break;
            default:
                geolocationListener.onGeolocationSuccess(locationToSearchFor);
                break;
        }
    }

    public interface GeolocationListener {

        Context getContext();

        void onGeolocationSuccess(String locationToSearchFor);

        void onGeolocationFaliure();
    }
}
