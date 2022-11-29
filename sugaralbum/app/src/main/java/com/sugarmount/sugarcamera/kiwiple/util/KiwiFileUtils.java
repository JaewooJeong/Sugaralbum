
package com.sugarmount.sugarcamera.kiwiple.util;

import android.content.Context;
import android.util.Log;

import com.sugarmount.sugaralbum.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;

public class KiwiFileUtils {
    private static final String TAG = KiwiFileUtils.class.getSimpleName();
    private static final int BUFFER_SIZE = 1024;

    public static void savePictureFile(Context context, byte[] data, String filename)
            throws Exception {
        BufferedOutputStream bos = null;
        try {
            Log.i(TAG, "SAVE CameraTakenPicture : " + filename);

            FileOutputStream fos = new FileOutputStream(filename, false);
            bos = new BufferedOutputStream(fos);
            bos.write(data);
            bos.flush();

            File file = new File(filename);
            if(file.exists()) {
                Log.i(TAG,
                      "spf(), Image Size : " + file.length() + "file directory : "
                              + file.getAbsolutePath());
            }
        } catch(OutOfMemoryError e) {
            String msg = context.getResources().getString(R.string.out_of_memory);
            System.gc();
            Log.e(TAG, e.toString());
            throw new Exception(msg);
        } catch(FileNotFoundException e) {
            String msg = context.getResources().getString(R.string.file_not_found);
            Log.e(TAG, e.toString());
            throw new Exception(msg);
        } catch(Exception e) {
            Log.e(TAG, "savePictureFile", e);
        } finally {
            if(bos != null) {
                try {
                    bos.close();
                    bos = null;
                    System.gc();
                } catch(IOException e) {
                }
            }
        }
    }

    public static void copyFile(String source, String target) {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        FileChannel fcin = null;
        FileChannel fcout = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);

            fcin = inputStream.getChannel();
            fcout = outputStream.getChannel();

            long size = fcin.size();
            fcin.transferTo(0, size, fcout);
        } catch(FileNotFoundException e) {
        } catch(IOException e) {
        } finally {
            try {
                if(fcout != null) {
                    fcout.close();
                }
                if(fcin != null) {
                    fcin.close();
                }
                if(outputStream != null) {
                    outputStream.close();
                }
                if(inputStream != null) {
                    inputStream.close();
                }
            } catch(IOException e) {
            }
        }
    }

    public static void copyFile(String source, FileOutputStream outputStream) {
        FileInputStream inputStream = null;

        FileChannel fcin = null;
        FileChannel fcout = null;
        try {
            inputStream = new FileInputStream(source);
            fcin = inputStream.getChannel();
            fcout = outputStream.getChannel();

            long size = fcin.size();
            fcin.transferTo(0, size, fcout);
        } catch(FileNotFoundException e) {
        } catch(IOException e) {
        } finally {
            if(fcout != null) {
                try {
                    fcout.close();
                } catch(IOException e) {
                }
            }
            if(fcin != null) {
                try {
                    fcin.close();
                } catch(IOException e) {
                }
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch(IOException e) {
                }
            }
        }
    }

    public static void deleteDirectoryFiles(File file, boolean deleteDirectory) {
        if(file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            int size = files.length - 1;
            for(int i = size; i >= 0; i--) {
                files[i].delete();
            }
            if(deleteDirectory) {
                file.delete();
            }
        }
    }
    
    public static Object readSerializableData(String path) {
        File file = new File(path);
        Object data = null;
        if(!file.exists()) {
            return null;
        }
        ObjectInputStream stream = null;
        try {
            stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file),
                                                                   BUFFER_SIZE));
            data = stream.readObject();
        } catch(Throwable e) {
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException e) {
                }
            }
        }
        return data;
    }

    public static void writeSerializableData(Object data, String path) {
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(path),
                                                                     BUFFER_SIZE));
            stream.writeObject(data);
            stream.flush();
        } catch(Throwable e) {
        } finally {
            try {
                if(stream != null) {
                    stream.close();
                }
            } catch(IOException e) {
            }
        }
    }
}
