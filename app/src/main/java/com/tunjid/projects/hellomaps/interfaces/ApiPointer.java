package com.tunjid.projects.hellomaps.interfaces;


import com.tunjid.projects.hellomaps.model.response.LocationsResponse;

import retrofit.http.GET;
import rx.Observable;

/**
 * REST API Interface for HelloWorld
 */
@SuppressWarnings("unused")
public interface ApiPointer {

    /**
     * Gets Locations from the Api
     */
    @GET("/helloworld_locations.json")
    Observable<LocationsResponse> getLocations();

}
