
package com.sugarmount.sugarcamera.kiwiple.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.hardware.Camera.Size;
import android.location.Location;
import android.media.ExifInterface;

import com.sugarmount.sugarcamera.kiwiple.Global;

public class JpegFileUtils {
    private static final String TAG = JpegFileUtils.class.getSimpleName();

    /**
     * Desc : 이미지에 메타데이터(EXIF) 저장
     * 
     * @Method Name : saveJpegMetaData
     * @param p 좌표
     * @param original 원본이미지
     * @param rotation 이미지 방향
     * @return
     */
    public static String saveJpegMetaData(Context context, Location location, String original,
            int rotation, Size size, long current) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(original);

            SmartLog.getInstance().e(TAG,
                                     "before date : "
                                             + exif.getAttribute(ExifInterface.TAG_DATETIME));
            SmartLog.getInstance().i(TAG,
                                     "Original Orientation : "
                                             + exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                                                    -1));

            if(rotation == 90 || rotation == -270) {
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, ""
                        + ExifInterface.ORIENTATION_ROTATE_90);
                SmartLog.getInstance().i(TAG,
                                         "Set Orientation: " + ExifInterface.ORIENTATION_ROTATE_90);
            } else if(rotation == 180 || rotation == -180) {
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, ""
                        + ExifInterface.ORIENTATION_ROTATE_180);
                SmartLog.getInstance()
                        .i(TAG, "Set Orientation: " + ExifInterface.ORIENTATION_ROTATE_180);
            } else if(rotation == 270 || rotation == -90) {
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, ""
                        + ExifInterface.ORIENTATION_ROTATE_270);
                SmartLog.getInstance()
                        .i(TAG, "Set Orientation: " + ExifInterface.ORIENTATION_ROTATE_270);
            } else {
                exif.setAttribute(ExifInterface.TAG_ORIENTATION, ""
                        + ExifInterface.ORIENTATION_NORMAL);
                SmartLog.getInstance().i(TAG,
                                         "Set Orientation: " + ExifInterface.ORIENTATION_NORMAL);
            }

            if(location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                SmartLog.getInstance().i(TAG, "mylocation latitude : " + latitude);
                SmartLog.getInstance().i(TAG, "mylocation longitude : " + longitude);

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude > 0 ? "N" : "S");
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude > 0 ? "E" : "W");
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, makeLatLongString(latitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, makeLatLongString(longitude));
            }
            if(size != null) {
                if(rotation == 90 || rotation == -270 || rotation == 270 || rotation == -90) {
                    exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(size.height));
                    exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(size.width));
                } else {
                    exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(size.width));
                    exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(size.height));
                }
            }

            SimpleDateFormat exifFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            exif.setAttribute(ExifInterface.TAG_DATETIME, exifFormatter.format(new Date(current))); // set
                                                                                                    // the
                                                                                                    // date
                                                                                                    // &
                                                                                                    // time
            exif.saveAttributes();

            // 원본이미지 메타데이터 저장
            Global.getInstance(context).setOriginalExif(exif);
        } catch(IOException e) {
        }
        return original;
    }

    private static String makeLatLongString(double d) {
        d = Math.abs(d);
        int degrees = (int)d;
        double remainder = d - degrees;
        int minutes = (int)(remainder * 60D);
        int seconds = (int)(((remainder * 60D) - minutes) * 60D * 1000D);
        String retVal = degrees + "/1," + minutes + "/1," + seconds + "/1000";
        return retVal;
    }

    /**
     * Desc : 원본 이미지 파일에서 추출한 exif 정보를 필터 저장시 저장해준다.
     * 
     * @Method Name : copExifInfo
     * @param src
     * @param dest
     */
    public static void copyExifInfo(Context context, ExifInterface src, ExifInterface dest,
            long current, boolean withOrientation) {
        if(src != null && dest != null) {
            for(Field f : ExifInterface.class.getFields()) {
                String name = f.getName();
                if(!name.startsWith("TAG_")) {
                    continue;
                }
                // boolean useLocationInfo =
                // MhkPreferenceManager.getInstance(context).isSaveLoactionInfo();
                boolean useLocationInfo = false;

                if(!useLocationInfo) {
                    if(name.startsWith("TAG_GPS_")) {
                        continue;
                    }
                }

                String key = null;
                try {
                    key = (String)f.get(null);
                } catch(Exception e) {
                    continue;
                }
                if(key == null) {
                    continue;
                }

                // 이미지 사이즈, 이지미 방향은 제외
                // TASK: GPS 시간 정보도 제외(GPS 시간 정보를 저장하면 사진 시간이 과거로 설정되는 현상이 있음, 검토 필요....)
                if(key.equals(ExifInterface.TAG_IMAGE_LENGTH)
                        || key.equals(ExifInterface.TAG_IMAGE_WIDTH)
                        || (key.equals(ExifInterface.TAG_ORIENTATION) && !withOrientation)
                        || key.equals(ExifInterface.TAG_GPS_DATESTAMP)) {
                    continue;
                }

                String value = src.getAttribute(key);
                if(value == null) {
                    continue;
                }
                dest.setAttribute(key, value);
            }
        }

        try {
            // 시간 정보를 업데이트 해준다.
            if(current != 0 && dest != null) {
                SimpleDateFormat exifFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                dest.setAttribute(ExifInterface.TAG_DATETIME,
                                  exifFormatter.format(new Date(current))); // set the date & time
            }
            dest.saveAttributes();
        } catch(Exception e) {
        }
    }
}
