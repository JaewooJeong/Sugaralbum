
package com.kiwiple.imageframework.burstshot;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class BurstShotUtils {
    private static String TAG = "BurstShotUtils";

    // save the raw preview image
    public static String saveYuvPreview(Context context, String filename, byte[] data) {
        File file = null;
        String path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator
                + "gif";
        String savedFilePath = path + File.separator + filename;

        File dirFile = new File(path);

        if(dirFile.exists() == false) {
            dirFile.mkdir();
        }

        BufferedOutputStream bos = null;
        try {
            file = new File(savedFilePath);
            FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data);
        } catch(IOException e) {
            savedFilePath = null;
            Log.e(TAG, "IOException in savedata", e);
        } finally {
            if(bos != null) {
                try {
                    bos.close();
                } catch(IOException e1) {
                    // do nothing
                }
            }
        }

        return savedFilePath;
    }

    public static Bitmap decodingImage(String filename, int size, Bitmap.Config config)
            throws IOException {
        File ori = new File(filename);
        if(!ori.exists()) {
            throw new FileNotFoundException();
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, size);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = config;

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeFile(filename, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int size) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > size || width > size) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = (int)Math.ceil((float)height / (float)size);
            final int widthRatio = (int)Math.ceil((float)width / (float)size);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
