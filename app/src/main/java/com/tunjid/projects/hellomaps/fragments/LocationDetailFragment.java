package com.tunjid.projects.hellomaps.fragments;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import com.tunjid.projects.hellomaps.R;
import com.tunjid.projects.hellomaps.abstractclasses.CoreFragment;
import com.tunjid.projects.hellomaps.activities.LocationActivity;
import com.tunjid.projects.hellomaps.customcomponents.Utils;
import com.tunjid.projects.hellomaps.model.pojo.Location;
import com.tunjid.projects.hellomaps.picassotransformations.RoundedTransformation;
import com.tunjid.projects.hellomaps.services.HelloApi;

public class LocationDetailFragment extends CoreFragment
        implements
        View.OnClickListener,
        com.google.android.gms.maps.OnMapReadyCallback {

    private Location location;
    private MapView mapView;
    private GoogleMap map;
    private android.location.Location googleLocation;

    public static LocationDetailFragment newInstance(String tag, Location location) {
        LocationDetailFragment fragment = new LocationDetailFragment();
        Bundle args = new Bundle();
        args.putString(Utils.FRAGMENT_TAG, tag);
        args.putParcelable(Utils.DYNAMIC_LOCATION, location);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        location = args.getParcelable(Utils.DYNAMIC_LOCATION);

        if (savedInstanceState != null) {
            googleLocation = savedInstanceState.getParcelable(Utils.GOOGLE_LOCATION);
        }
        else {
            googleLocation = args.getParcelable(Utils.GOOGLE_LOCATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_detail, container, false);

        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle("mapViewSaveState") : null;

        initializeViewComponents(rootView, mapViewSavedInstanceState);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Once the fragment's view is created, instruct the activity.
        callAfterActivityCreated();
    }

    @Override
    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onLowMemory() {
        if (mapView != null) {
            mapView.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onPause() {

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onDestroyView() {

        if (map != null) {
            map.clear();
            map.setMyLocationEnabled(false);
        }
        if (mapView != null) {
            mapView.onDestroy();
        }

        mapView = null;

        View rootView = getView();

        if (rootView != null) {
            SlidingUpPanelLayout mapViewContainer =
                    (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);

            if (mapViewContainer != null) {
                mapViewContainer.removeAllViews();
            }
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mapView != null) {
            mapView.onDestroy();
        }
        super.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {

        //This MUST be done before saving any of your own or your base class's variables
        final Bundle mapViewSaveState = new Bundle(outState);

        if (mapView != null) { // May be null after onDestroView
            mapView.onSaveInstanceState(mapViewSaveState);
            outState.putBundle("mapViewSaveState", mapViewSaveState);
        }

        outState.putParcelable(Utils.GOOGLE_LOCATION, googleLocation);
        super.onSaveInstanceState(outState);
    }

    private void initializeViewComponents(View rootView, Bundle savedInstanceState) {

        final TextView officeName = (TextView) rootView.findViewById(R.id.office_name);
        final TextView officeAddress = (TextView) rootView.findViewById(R.id.office_address);
        final TextView distance = (TextView) rootView.findViewById(R.id.distance);
        final Button call = (Button) rootView.findViewById(R.id.call);
        final Button directions = (Button) rootView.findViewById(R.id.directions);
        final ImageView officePhoto = (ImageView) rootView.findViewById(R.id.office_photo);

        officeName.setText(location.getName() != null ? location.getName() : "");
        officeAddress.setText(location.getFullAddress());
        distance.setText(location.formattedDistanceFrom(HelloApi.getUserLastLocation()));

        call.setOnClickListener(this);
        directions.setOnClickListener(this);

        Picasso.with(getContext())
                .load(location.getOfficeImage())
                .placeholder(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.grey_600)))
                .transform(new RoundedTransformation(getResources()
                        .getDimensionPixelSize(R.dimen.quarter_margin), 0))
                .fit()
                .into(officePhoto);

        mapView = (MapView) rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(this.getActivity());

        this.map = googleMap;

        // Configure map
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.setMyLocationEnabled(true);

        setupFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call:
                makePhoneCall(location.getPhone());
                break;
            case R.id.directions:
                if (((LocationActivity) getActivity()).isNetworkAvailable()) {
                    navigateToLocation(location);
                }
                else {
                    Toast.makeText(getContext(), R.string.please_connect, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void setupFragment() {

        if (map != null) { // Map loaded
            try {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker);

                LatLng latLng = new LatLng(location.getLat(), location.getLong());

                map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(location.getName())
                        .icon(icon)
                        .snippet(location.getAddress()));

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);

                map.animateCamera(cameraUpdate);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    private void navigateToLocation(com.tunjid.projects.hellomaps.model.pojo.Location location) {

        Uri gmmNavIntentUri = Uri.parse("google.navigation:q=" +
                location.getLatitude() + "," +
                location.getLongitude());

        Intent mapNavIntent = new Intent(Intent.ACTION_VIEW, gmmNavIntentUri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mapNavIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        else {
            mapNavIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        mapNavIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapNavIntent);
    }

}
