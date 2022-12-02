
package com.kiwiple.imageframework.network.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.imageframework.util.SmartLog;

public class NetUtils {
    private static final String TAG = "NetUtils";
    private static final int BUFFER_SIZE = 1024 * 300;

    private static final byte[] mByteBuffer;

    static {
        mByteBuffer = new byte[BUFFER_SIZE];
    }

    public static Boolean ParseErrorCode(JSONObject json) {
        try {
            Integer errorCode = json.getInt("ErrorCode");
            String errorMsg = json.getString("ErrorMessage");
            SmartLog.d(TAG, "ERRORCODE:" + errorCode + " ,MSG:" + errorMsg);

            if(errorCode == 0) {
                return true;
            } else {
                return false;
            }
        } catch(JSONException e) {
            SmartLog.e(TAG, e.toString());
            return false;
        }
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    public static ByteBuffer convertStreamToByteBuffer(InputStream is) {

        int i = 0;
        int count = 0;

        try {
            while((count = is.read(mByteBuffer, i, BUFFER_SIZE - i)) != -1) {
                i += count;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        SmartLog.i(TAG, "Buffer size : " + i);
        ByteBuffer byteBuffer = ByteBuffer.allocate(i);
        byteBuffer.put(mByteBuffer, 0, i);
        byteBuffer.position(0);
        // byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        return byteBuffer;
    }

    public static Double getLatitudeMin(double latitude, double distance) {
        Double aLatitude = latitude - distance * 0.01;
        if(aLatitude < 0)
            aLatitude = 0.0;
        return aLatitude;
    }

    public static Double getLatitudeMax(double latitude, double distance) {
        Double aLatitude = latitude + distance * 0.01;
        if(aLatitude > 90)
            aLatitude = 90.0;
        return aLatitude;
    }

    public static Double getLongitudeMin(double longitude, double distance) {
        Double aLongitude = longitude - distance * 0.01;
        if(aLongitude < 0)
            aLongitude = 0.0;
        return aLongitude;
    }

    public static Double getLongitudeMax(double longitude, double distance) {
        Double aLongitude = longitude + distance * 0.01;
        if(aLongitude > 360)
            aLongitude = 360.0;
        // aLongitude = 360.0;
        return aLongitude;
    }
}
