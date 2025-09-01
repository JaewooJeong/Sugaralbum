package com.sugarmount.sugarcamera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.kiwiple.imageframework.util.SmartLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper class for Android 15 scoped storage compatibility
 * Handles video file creation using MediaStore API
 */
public class MediaStoreHelper {
    private static final String TAG = "MediaStoreHelper";
    
    /**
     * Create a video file using MediaStore API (Android 15 compatible)
     * @param context Application context
     * @param displayName File display name (without extension)
     * @param mimeType MIME type (e.g., "video/mp4")
     * @return Uri of the created file, or null if failed
     */
    public static Uri createVideoFile(Context context, String displayName, String mimeType) {
        ContentResolver resolver = context.getContentResolver();
        
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Video.Media.MIME_TYPE, mimeType);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/SugarAlbum");
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        }
        
        try {
            Uri videoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            SmartLog.i(TAG, "Created video file URI: " + videoUri);
            return videoUri;
        } catch (Exception e) {
            SmartLog.e(TAG, "Failed to create video file", e);
            return null;
        }
    }
    
    /**
     * Finalize video file after writing is complete
     * @param context Application context
     * @param videoUri Uri returned from createVideoFile
     * @param fileSize Final file size in bytes
     */
    public static void finalizeVideoFile(Context context, Uri videoUri, long fileSize) {
        if (videoUri == null) return;
        
        ContentResolver resolver = context.getContentResolver();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            values.put(MediaStore.Video.Media.SIZE, fileSize);
            
            try {
                resolver.update(videoUri, values, null, null);
                SmartLog.i(TAG, "Finalized video file: " + videoUri);
            } catch (Exception e) {
                SmartLog.e(TAG, "Failed to finalize video file", e);
            }
        }
    }
    
    /**
     * Get output stream for writing to MediaStore URI
     * @param context Application context  
     * @param videoUri Uri returned from createVideoFile
     * @return OutputStream for writing, or null if failed
     */
    public static OutputStream getOutputStream(Context context, Uri videoUri) {
        if (videoUri == null) return null;
        
        try {
            ContentResolver resolver = context.getContentResolver();
            return resolver.openOutputStream(videoUri);
        } catch (Exception e) {
            SmartLog.e(TAG, "Failed to get output stream", e);
            return null;
        }
    }
    
    /**
     * Fallback method for older Android versions or when MediaStore fails
     * Creates file in app-specific external directory
     */
    public static String createFallbackVideoPath(Context context, String fileName) {
        File moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (moviesDir == null) {
            // Use internal storage as last resort
            moviesDir = new File(context.getFilesDir(), "movies");
        }
        
        File sugarAlbumDir = new File(moviesDir, "SugarAlbum");
        if (!sugarAlbumDir.exists()) {
            sugarAlbumDir.mkdirs();
        }
        
        File videoFile = new File(sugarAlbumDir, fileName);
        SmartLog.i(TAG, "Fallback video path: " + videoFile.getAbsolutePath());
        return videoFile.getAbsolutePath();
    }
    
    /**
     * Get file path from MediaStore URI (for video processing)
     * On Android 15, we can't get direct file paths from MediaStore URIs
     * Instead, we create a temporary file in app-specific storage
     * @param context Application context
     * @param uri MediaStore URI
     * @return File path for temporary processing or null if failed
     */
    public static String getFilePathFromUri(Context context, Uri uri) {
        if (uri == null) return null;
        
        try {
            // Create a temporary file in app-specific storage for video processing
            File tempDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "temp");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            String fileName = "video_" + System.currentTimeMillis() + ".tmp";
            File tempFile = new File(tempDir, fileName);
            
            SmartLog.i(TAG, "Created temp file path for MediaStore URI: " + tempFile.getAbsolutePath());
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            SmartLog.e(TAG, "Failed to create temp file path", e);
            return null;
        }
    }
    
    /**
     * Copy video file to MediaStore URI after processing
     * @param context Application context
     * @param sourceFilePath Source file path (from temp directory)
     * @param targetUri MediaStore URI
     * @return true if successful
     */
    public static boolean copyVideoToMediaStore(Context context, String sourceFilePath, Uri targetUri) {
        if (sourceFilePath == null || targetUri == null) return false;
        
        try {
            File sourceFile = new File(sourceFilePath);
            if (!sourceFile.exists()) {
                SmartLog.e(TAG, "Source file does not exist: " + sourceFilePath);
                return false;
            }
            
            ContentResolver resolver = context.getContentResolver();
            OutputStream outputStream = resolver.openOutputStream(targetUri);
            if (outputStream == null) {
                SmartLog.e(TAG, "Cannot open OutputStream for URI: " + targetUri);
                return false;
            }
            
            java.io.FileInputStream inputStream = new java.io.FileInputStream(sourceFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.close();
            
            SmartLog.i(TAG, "Successfully copied video to MediaStore: " + targetUri);
            return true;
        } catch (Exception e) {
            SmartLog.e(TAG, "Failed to copy video to MediaStore", e);
            return false;
        }
    }
    
    /**
     * Add existing video file to MediaStore (for fallback path)
     */
    public static Uri addVideoToMediaStore(Context context, String filePath, String displayName) {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

        // Use RELATIVE_PATH for Android Q and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/SugarAlbum");
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        } else {
            // For older versions, use the file path directly (less secure)
            values.put(MediaStore.Video.Media.DATA, filePath);
        }

        Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri videoUri = null;

        try {
            // For modern Android, insert first to get a URI, then write data.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                videoUri = resolver.insert(collection, values);
                if (videoUri == null) {
                    throw new IOException("Failed to create new MediaStore record.");
                }

                try (OutputStream out = resolver.openOutputStream(videoUri)) {
                    if (out == null) {
                        throw new IOException("Failed to get output stream.");
                    }
                    File sourceFile = new File(filePath);
                    try (java.io.FileInputStream in = new java.io.FileInputStream(sourceFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                }

                values.clear();
                values.put(MediaStore.Video.Media.IS_PENDING, 0);
                resolver.update(videoUri, values, null, null);
            } else {
                // For older Android, the DATA path is already in values, just insert.
                videoUri = resolver.insert(collection, values);
            }


            SmartLog.i(TAG, "Added video to MediaStore: " + videoUri);
            return videoUri;

        } catch (Exception e) {
            SmartLog.e(TAG, "Failed to add video to MediaStore", e);
            // If something went wrong, delete the incomplete MediaStore entry
            if (videoUri != null) {
                resolver.delete(videoUri, null, null);
            }
            return null;
        }
    }
}