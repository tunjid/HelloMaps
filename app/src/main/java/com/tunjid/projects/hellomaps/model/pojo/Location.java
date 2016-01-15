package com.tunjid.projects.hellomaps.model.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tunjid.projects.hellomaps.customcomponents.Utils;

import java.util.Comparator;

import lombok.Getter;
import lombok.Setter;

/**
 * Hello Location POJO Object
 */

@Getter
@Setter
public class Location implements Parcelable {

    String name;
    String address;
    String address2;
    String city;
    String state;

    String phone;
    String fax;
    String latitude;
    String longitude;
    @SerializedName("office_image")
    String officeImage;
    @SerializedName("zip_postal_code")
    String zipCode;

    public String getFullAddress() {
        String fullAddress = address != null ? address : "";
        fullAddress += "\n";
        fullAddress += address2 != null ? address2 : "";
        fullAddress += "\n";
        fullAddress += city != null ? city : "";
        fullAddress += ", ";
        fullAddress += state != null ? state : "";
        fullAddress += "\n";
        fullAddress += zipCode != null ? zipCode : "";

        return fullAddress;
    }

    public String formattedDistanceFrom(android.location.Location location) {
        if (this.latitude == null || this.longitude == null || location == null) {
            return "";
        }
        else {
            double distance = this.distanceFrom(location);
            return distance + " mi away";
        }
    }

    public double distanceFrom(android.location.Location location) {
        if (this.latitude == null || this.longitude == null || location == null) {
            return 0.0D;
        }
        else {
            try {
                double latitude = Double.parseDouble(this.latitude);
                double longitude = Double.parseDouble(this.longitude);
                float[] results = new float[4];
                android.location.Location.distanceBetween(location.getLatitude(), location.getLongitude(), latitude, longitude, results);

                // Convert to miles
                return Utils.round((double) results[0] * 6.2E-4D, 1);
            }
            catch (Exception e) {
                e.printStackTrace();
                return 0.0D;
            }
        }
    }

    public double getLat() {
        try {
            return Double.parseDouble(this.latitude);
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0.0D;
        }
    }

    public double getLong() {
        try {
            return Double.parseDouble(this.longitude);
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0.0D;
        }
    }

    public static Comparator<Location> LocationComparator(final android.location.Location googleLocation) {
        return new Comparator<Location>() {
            @Override
            public int compare(Location item1, Location item2) {
                int i;

                String name1 = item1.getName() != null ? item1.getName() : "a";
                String name2 = item2.getName() != null ? item2.getName() : "b";

                if (googleLocation != null) {
                    Double distance1 = item1.distanceFrom(googleLocation);
                    Double distance2 = item2.distanceFrom(googleLocation);
                    i = distance1.compareTo(distance2);
                }
                else { // Contigency in case locations are unavailable
                    i = name1.compareTo(name2);
                }
                return i;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Location)) {
            return false;
        }

        Location location = (Location) o;

        if (name != null ? !name.equals(location.name) : location.name != null) {
            return false;
        }
        if (address != null ? !address.equals(location.address) : location.address != null) {
            return false;
        }
        if (address2 != null ? !address2.equals(location.address2) : location.address2 != null) {
            return false;
        }
        if (city != null ? !city.equals(location.city) : location.city != null) {
            return false;
        }
        if (state != null ? !state.equals(location.state) : location.state != null) {
            return false;
        }
        if (phone != null ? !phone.equals(location.phone) : location.phone != null) {
            return false;
        }
        if (fax != null ? !fax.equals(location.fax) : location.fax != null) {
            return false;
        }
        if (latitude != null ? !latitude.equals(location.latitude) : location.latitude != null) {
            return false;
        }
        if (longitude != null ? !longitude.equals(location.longitude) : location.longitude != null) {
            return false;
        }
        if (officeImage != null ? !officeImage.equals(location.officeImage) : location.officeImage != null) {
            return false;
        }
        return !(zipCode != null ? !zipCode.equals(location.zipCode) : location.zipCode != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (fax != null ? fax.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (officeImage != null ? officeImage.hashCode() : 0);
        return result;
    }

    protected Location(Parcel in) {
        name = in.readString();
        address = in.readString();
        address2 = in.readString();
        city = in.readString();
        state = in.readString();
        phone = in.readString();
        fax = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        officeImage = in.readString();
        zipCode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(address2);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(phone);
        dest.writeString(fax);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(officeImage);
        dest.writeString(zipCode);
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
}
