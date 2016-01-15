package com.tunjid.projects.hellomaps.model.response;

import com.tunjid.projects.hellomaps.interfaces.Observation;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>Base clase that contains the inner class Meta. </p>
 * <p>Meta simply holds the result code for a HTTP request.
 * The hierachy of the class is so because of the the JSON recieved from the request.</p>
 */
@Getter
public class BaseResponse implements Observation {
    transient int id;
    @Setter transient ArrayList<Object> metaData = new ArrayList<>();

    @Override
    public void setID(int id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }

    public BaseResponse() {

    }
}
