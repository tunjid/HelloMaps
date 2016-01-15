package com.tunjid.projects.hellomaps.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tunjid.projects.hellomaps.R;
import com.tunjid.projects.hellomaps.abstractclasses.CoreAdapter;
import com.tunjid.projects.hellomaps.abstractclasses.CoreFragment;
import com.tunjid.projects.hellomaps.activities.LocationActivity;
import com.tunjid.projects.hellomaps.adapters.LocationsAdapter;
import com.tunjid.projects.hellomaps.customcomponents.Utils;
import com.tunjid.projects.hellomaps.interfaces.Observation;
import com.tunjid.projects.hellomaps.model.response.LocationsResponse;
import com.tunjid.projects.hellomaps.services.HelloApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LocationFragment extends CoreFragment
        implements
        SwipeRefreshLayout.OnRefreshListener,
        com.google.android.gms.maps.OnMapReadyCallback {


    private final static String LOCATIONS = "LOCATIONS";

    private static final int LOCATION_DATA = 1;

    private ArrayList<com.tunjid.projects.hellomaps.model.pojo.Location> locations = new ArrayList<>();
    private Location googleLocation;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MapView mapView;
    private GoogleMap map;

    public static LocationFragment newInstance(String tag) {
        LocationFragment fragment = new LocationFragment();
        Bundle args = new Bundle();
        args.putString(Utils.FRAGMENT_TAG, tag);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            locations = savedInstanceState.getParcelableArrayList(LOCATIONS);
            googleLocation = savedInstanceState.getParcelable(Utils.GOOGLE_LOCATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_locations, container, false);

        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle("mapViewSaveState") : null;

        initializeViewComponents(rootView, mapViewSavedInstanceState);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            refresh();
        }
        // View destroyed, but fragment wasn't
        else if (viewDestroyed()) {
            refresh();
        }
        else {
            if (((LocationActivity) getActivity()).isNetworkAvailable()) {
                getLocations();
            }
            else { // Retrive cached locations
                SharedPreferences sharedPreferences
                        = getActivity().getSharedPreferences(Utils.PREFS, Context.MODE_PRIVATE);

                Set<String> cachedLocations = sharedPreferences.getStringSet(Utils.CACHED_LOCATIONS, new HashSet<String>());

                locations.addAll(Utils.createModelList(cachedLocations, com.tunjid.projects.hellomaps.model.pojo.Location.class));
                refresh();
            }
        }

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
    public void onDetach() {
        super.onDetach();
        if (apiSubscription != null) {
            apiSubscription.unsubscribe();
        }
    }

    @Override
    public void reconstructView() {
        super.reconstructView();
        ((CoreAdapter) recyclerView.getAdapter()).refreshData();
    }

    @Override
    public void deconstructView() {
        super.deconstructView();

        if (recyclerView != null) {
            ((CoreAdapter) recyclerView.getAdapter()).onDeconstructView();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (recyclerView != null) {
            // Detaches the adapter from the RecyclerView
            recyclerView.setAdapter(null);
        }

        recyclerView = null;
        swipeRefreshLayout = null;

        // Prevent Map related memory leaks
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

            RelativeLayout mapViewContainer =
                    (RelativeLayout) rootView.findViewById(R.id.map_view_container);

            if (mapViewContainer != null) {
                mapViewContainer.removeAllViews();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {

        //This MUST be done before saving any base class's variables
        final Bundle mapViewSaveState = new Bundle(outState);

        if (mapView != null) { // May be null after onDestroView
            mapView.onSaveInstanceState(mapViewSaveState);
            outState.putBundle("mapViewSaveState", mapViewSaveState);
        }

        //Add any other variables here.
        outState.putParcelableArrayList(LOCATIONS, locations);
        outState.putParcelable(Utils.GOOGLE_LOCATION, googleLocation);

        super.onSaveInstanceState(outState);
    }

    private void initializeViewComponents(View rootView, Bundle savedInstanceState) {

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        final LinearLayoutManager recylerViewLayoutManager = new LinearLayoutManager(getActivity());
        recylerViewLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recylerViewLayoutManager.scrollToPosition(0);

        final LocationsAdapter locationsAdapter = new LocationsAdapter(locations);
        locationsAdapter.setAdapterListener(new LocationsAdapter.AdapterListener() {
            @Override
            public void onLocationClicked(com.tunjid.projects.hellomaps.model.pojo.Location location) {
                ((LocationActivity) getActivity()).onLocationClicked(location);
            }
        });

        recyclerView.setLayoutManager(recylerViewLayoutManager);
        recyclerView.setAdapter(locationsAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        mapView = (MapView) rootView.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    public void refresh() {
        if (recyclerView != null) {
            if (HelloApi.getUserLastLocation() != null) {
                Collections.sort(locations, com.tunjid.projects.hellomaps.model.pojo.Location.LocationComparator(HelloApi.getUserLastLocation()));
            }
            ((CoreAdapter) recyclerView.getAdapter()).refreshData();
        }
    }

    public void refreshIfLoaded() {
        if (recyclerView != null) {
            boolean isNotLoadingData = !(((CoreAdapter) recyclerView.getAdapter()).isLoadingData());
            if (isNotLoadingData) {
                Collections.sort(locations, com.tunjid.projects.hellomaps.model.pojo.Location.LocationComparator(HelloApi.getUserLastLocation()));
                ((CoreAdapter) recyclerView.getAdapter()).refreshData();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(this.getActivity());

        this.map = googleMap;

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.setMyLocationEnabled(true);

        if (viewDestroyed()) {
            populateMapView();
        }
    }

    private void populateMapView() {
        if (map != null) {
            try {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker);

                // put markers for all locations
                for (com.tunjid.projects.hellomaps.model.pojo.Location location : locations) {

                    if (location.getLatitude() != null && location.getLongitude() != null) {

                        double latitude = location.getLat();
                        double longitude = location.getLong();

                        LatLng latLng = new LatLng(latitude, longitude);

                        map.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(location.getName())
                                .icon(icon)
                                .snippet(location.getAddress()));

                        builder.include(latLng);
                    }
                }

                zoomMap(builder);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void zoomMap(LatLngBounds.Builder builder) {

        boolean animate = !viewDestroyed();

        if (map != null) {

            LatLngBounds bounds = builder.build();

            CameraUpdate cameraUpdate;

            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,
                    getResources().getDimensionPixelSize(R.dimen.triple_and_half_margin));


            if (animate) {
                map.animateCamera(cameraUpdate);
            }
            else {
                map.moveCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        showErrorDialog();
        refresh();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        e.printStackTrace();
    }

    @Override
    public void onNext(Observation observation) {
        switch (observation.getID()) {
            case LOCATION_DATA:

                swipeRefreshLayout.setRefreshing(false);
                LocationsResponse locationsResponse = (LocationsResponse) observation;


                Set<String> locationHashSetString
                        = new HashSet<>();

                SharedPreferences sharedPreferences
                        = getActivity().getSharedPreferences(Utils.PREFS, Context.MODE_PRIVATE);

                locations.clear();

                // Add to locations list and cache
                for (com.tunjid.projects.hellomaps.model.pojo.Location location : locationsResponse.getLocations()) {
                    if (location != null) {
                        locations.add(location);
                        locationHashSetString.add(Utils.modelToString(location));
                    }
                }

                // Save retrieved locations locally
                sharedPreferences.edit().putStringSet(Utils.CACHED_LOCATIONS, locationHashSetString).apply();

                googleLocation = HelloApi.getUserLastLocation();

                refresh();
                populateMapView();
                break;

        }
    }

    @Override
    public void onRefresh() {
        getLocations();
    }

    /**
     * Gets locations from the RESTful API
     */
    public void getLocations() {

        HelloApi.getLocations()
                .flatMap(new Func1<LocationsResponse, Observable<Observation>>() {
                    @Override // Override Func1 with a custom function
                    public Observable<Observation> call(LocationsResponse locationsResponse) {
                        return Utils.observeResponse(LOCATION_DATA, locationsResponse);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(12, TimeUnit.SECONDS)
                .subscribe(this);
    }
}
