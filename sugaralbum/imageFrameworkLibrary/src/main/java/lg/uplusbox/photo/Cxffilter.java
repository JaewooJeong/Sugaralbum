//------------------------------------------------------------------------------
//
//  cxffilter Engine Interface for Android (Jni)
//
//
//  http://blog.naver.com/simonsayz  
//  simonsayz@naver.com
//  최원식옹
//
//  주의 : Memory Function의 Bitmap Format은 항상 ARGB_8888 DWord Type
//         다른 포맷 사용 금지
//
//------------------------------------------------------------------------------

package lg.uplusbox.photo;

import android.graphics.Bitmap;

public class Cxffilter {
    // Load cxf Filter Library
    static {
        System.loadLibrary("cxffilter");
    }

    // Common Function
    public native int autoFix(String inName, String outName, int scale, int preserved);

    public native String[] autoFixEx(String[] inName, String[] outName, int scale, int preserved);

    public native int aniGif(String[] inNames, String outGifName, int scale, int interval);

    public native int aniGifWH(String[] inNames, String outGifName, int width, int height,
            int interval);

    public native int collage(String[] inNames, String outName, int width, int height, int layout);

    public native int collageWithFrame(String[] inNames, String outName, int width, int height,
            String frame);

    public native String highLight(String inName, boolean optSharpness, boolean optFaceCount,
            boolean optFaceCut);

    public native String[] highLightEx(String[] inNames, boolean optSharpness,
            boolean optFaceCount, boolean optFaceCut);

    public native String[] highLightColorSetGet(String[] inName, boolean optSharpness,
            boolean optFaceCount, boolean optFaceCut);

    public native String colorSetGet(String inName);

    public native String colorSetCompare(String matA, String matB);

    // Memory Function ( In,Out Bmp = ARGB_8888 )
    public native int autoFixMem(Bitmap bmp);

    public native int aniGifMem(Bitmap[] bmps, String outGifName, int interval);

    public native int collageMem(Bitmap[] bmps, Bitmap outbmp, int layout);

    public native int collageWithFrameMem(Bitmap[] bmps, Bitmap outbmp, String frame);

    public native String highLightMem(Bitmap bmp, boolean optSharpness, boolean optFaceCount,
            boolean optFaceCut);

    public native String colorSetGetMem(Bitmap bmp);

    // Utility Function
    public native int saveToBmpMem(Bitmap bmp, String outBmpName);

    public native int saveToGifMem(Bitmap bmp, String outGifName);

    // Version Function
    public native int getBuildNo();

    public native String getVersion();

}
