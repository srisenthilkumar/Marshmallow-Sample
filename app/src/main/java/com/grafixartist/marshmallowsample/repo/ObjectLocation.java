package com.grafixartist.marshmallowsample.repo;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.grafixartist.marshmallowsample.model.DeviceLocation;
import com.grafixartist.marshmallowsample.util.RestClient;

/**
 * Created by salagumalai on 07-05-2016.
 */
public class ObjectLocation {
    private static final String TAG = "ObjectLocation";

    public DeviceLocation getLocationByDevice(){
        //setting header to request for a JSON response
        RestClient client = new RestClient(null);
        String locationJSONStr = client.getHttpResponse();
        Log.d(TAG, "Response: " + locationJSONStr );
        return convertJson(locationJSONStr);
    }

    private DeviceLocation convertJson(String productJSONStr) {
        DeviceLocation location = null;
        if (productJSONStr != null && productJSONStr.length() > 0) {
            try {
                Gson gson = new Gson();
                location =
                        gson.fromJson(productJSONStr, new TypeToken<DeviceLocation>() {
                        }.getType());
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return location;
    }
}
