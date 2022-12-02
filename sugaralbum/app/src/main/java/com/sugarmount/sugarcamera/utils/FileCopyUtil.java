package com.sugarmount.sugarcamera.utils;

import android.content.Context;

import com.kiwiple.debug.L;
import com.sugarmount.sugaralbum.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCopyUtil {
	
	public static boolean isFileExits(Context context, String fileName){
		String packagePath = context.getFilesDir().getAbsolutePath();
		String outroFilePath = new StringBuffer().append(packagePath).append(File.separator).append(fileName).toString();
		
		File outroFile = new File(outroFilePath); 
		if(outroFile.exists()){
			return true; 
		}else{
			return false; 
		}
	}
	
	public static void copyFile(Context context, String fileName) 
	{
	    InputStream in = context.getResources().openRawResource(+R.drawable.outro1_2_modi);
	    OutputStream out = null;

	    try
	    {
	        String packagePath = context.getFilesDir().getAbsolutePath();
			String newFileName = new StringBuffer().append(packagePath).append(File.separator).append(fileName).toString();
	        out = new FileOutputStream(newFileName);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1)
	        {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        L.e(e.getMessage());
	    }finally{
	        if(in!=null){
	            try {
	                in.close();
	            } catch (IOException e) {
	                L.e("Exception while closing input stream" + e);
	            }
	        }
	        if(out!=null){
	            try {
	                out.close();
	            } catch (IOException e) {
	            	L.e("Exception while closing output stream" + e);
	            }
	        }
	    }
	}

}
