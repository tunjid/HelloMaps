package com.tunjid.projects.hellomaps.model.response;

import com.tunjid.projects.hellomaps.model.pojo.Location;

import java.util.ArrayList;

import lombok.Getter;

/**
 * Wrapper for Results received from the Api.
 */

@Getter
public class LocationsResponse extends BaseResponse {
    ArrayList<Location> locations = new ArrayList<>();
}
