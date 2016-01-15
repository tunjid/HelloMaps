package com.tunjid.projects.hellomaps.customcomponents;

import com.google.gson.Gson;
import com.tunjid.projects.hellomaps.interfaces.Observation;
import com.tunjid.projects.hellomaps.model.response.BaseResponse;

import java.util.ArrayList;
import java.util.Set;

import jodd.bean.BeanCopy;
import rx.Observable;

/**
 * Constants used throughout the application
 */
public class Utils {

    public Utils() {

    }

    private static final Gson gson = new Gson();

    public static final String PREFS = "PREFS"; // Where files are saved

    public static final String LOCATION_UPDATE = "LOCATION_UPDATE";

    public static final String CACHED_LOCATIONS = "CACHED_LOCATIONS";
    public static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    public static final String WAS_PAUSED = "WAS_PAUSED";
    public static final String WAS_STOPPED = "WAS_STOPPED";
    public static final String VIEW_DECONSTRUCTED = "VIEW_DECONSTRUCTED";

    public static final String VIEW_DESTROYED = "VIEW_DESTROYED";

    public static final String ACTUAL_LOCATION = "CURRENT_LOCATION";
    public static final String DYNAMIC_LOCATION = "DYNAMIC_LOCATION";
    public static final String GOOGLE_LOCATION = "GOOGLE_LOCATION";

    public static final String TOOLBAR_TITLE = "TOOLBAR_TITLE";

    public static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";

    /**
     * Base URLs for requests
     */
    public static final String HELLO_BASE_URL = "https://helloworld.com";


    /**
     * Serializes a model object to a string for easy storage in shared preferences
     *
     * @param model The Object to be serialized.
     */
    public static String modelToString(Object model) {
        return gson.toJson(model);
    }

    /**
     * Deserializes a string into a model object after being retrieved from shared preferences
     * <p/>
     *
     * @param model         A serialized String.
     * @param classofObject The class the String should be serialized to.
     * @return an object specified by the class above.
     */
    public static <DeserializedModel> DeserializedModel stringToModel(String model, Class<DeserializedModel> classofObject) {
        return gson.fromJson(model, classofObject);
    }

    /**
     * <p>Deserializes a ser of strings into an ArrayList of a
     * model object after being retrieved from Shared Preferences.</p>
     * <p>It uses the method {@link #stringToModel(String, Class)} defined in the Constants class</p>
     *
     * @param deserializedModel A serialized  {@link Set Set} of strings.
     * @param classofObject     The class the Strings should be serialized to.
     * @return an {@link ArrayList ArrayList} of the original object.
     */
    public static <DeserializedModel> ArrayList<DeserializedModel> createModelList
    (Set<String> deserializedModel, Class<DeserializedModel> classofObject) {

        ArrayList<DeserializedModel> resultList = new ArrayList<>();
        for (String s : deserializedModel) {
            resultList.add(stringToModel(s, classofObject));
        }
        return resultList;
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static Observable<Observation> observeResponse(int id, BaseResponse baseResponse) {
        baseResponse.setID(id);

        return Observable.just((Observation) baseResponse);
    }

    public static BeanCopy beans(Object source, Object destination) {
        return BeanCopy.beans(source, destination);
    }
}
