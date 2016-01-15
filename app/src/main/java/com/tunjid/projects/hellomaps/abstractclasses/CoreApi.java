package com.tunjid.projects.hellomaps.abstractclasses;

import android.app.Service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tunjid.projects.hellomaps.customcomponents.Utils;
import com.tunjid.projects.hellomaps.interfaces.ApiPointer;
import com.tunjid.projects.hellomaps.model.response.LocationsResponse;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import rx.Observable;

/**
 * Base class for bound service that makes API calls.
 */

@SuppressWarnings("unused")
public abstract class CoreApi extends Service{

    // The Json used in the API has a field naming cheme most similar to that chosen below.
    protected static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY) // Field names are as is for the JSON
            .disableHtmlEscaping()
            .create();

    protected static final RestAdapter HelloAdapter = new RestAdapter.Builder() // Used for API requests
            .setConverter(new GsonConverter(gson))
            .setEndpoint(Utils.HELLO_BASE_URL)
            .setLogLevel(RestAdapter.LogLevel.NONE)
            .setLog(new AndroidLog("RETROFIT"))
            .build();

    protected static final ApiPointer apiPointer = HelloAdapter.create(ApiPointer.class);

    /**
     * Gets the HelloWorld Locations
     */
    public static Observable<LocationsResponse> getLocations() {
        return apiPointer.getLocations();
    }
}
