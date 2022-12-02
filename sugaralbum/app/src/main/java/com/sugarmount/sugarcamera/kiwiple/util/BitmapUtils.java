
package com.sugarmount.sugarcamera.kiwiple.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.PixelCanvas;
import com.kiwiple.multimedia.util.Size;

public class BitmapUtils {

    public static BitmapFactory.Options getImageFileOption(String imageFilePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageFilePath, options);
		return options; 
	}
    
	// PixelBuffer를 통한 bitmap
	public static Bitmap getBitmapImageFromPixelBuffer(Size size, PixelCanvas pixelCanvas) {

		int width = size.width;
		int height = size.height;

		Bitmap bitmap = null, scaledBitmap = null;
		try {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixelCanvas.intArray, 0, width, 0, 0, width, height);
			
			scaledBitmap = com.kiwiple.imageframework.util.BitmapUtils.resizeBitmap(bitmap, width/2, height/2);
			if(bitmap != scaledBitmap){
				bitmap.recycle();
				bitmap = null;
			}
			L.i("scale size : "+ scaledBitmap.getWidth() +", "+ scaledBitmap.getHeight() +", "+ (scaledBitmap.getWidth() * scaledBitmap.getHeight() * 4) / 1024 +"bytes" );
		} catch (Exception e) {
			e.printStackTrace();
			return bitmap;
		}
		return scaledBitmap;
	}
}
