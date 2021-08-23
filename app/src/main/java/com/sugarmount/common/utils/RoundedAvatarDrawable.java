package com.sugarmount.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * Created by Jaewoo on 2016-09-01.
 */
// 라운드 처리를 위하여 만든 클레스
public class RoundedAvatarDrawable implements Transformation {

    private PROCESS_TYPE process_type = PROCESS_TYPE.CIRCLE;

    enum PROCESS_TYPE{
        CIRCLE,
        ROUNDED
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size);
        int y = (source.getHeight() - size);

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r;
        if(process_type == PROCESS_TYPE.CIRCLE){
            r = size / 2f;
            canvas.drawCircle(r, r, r, paint); //둥글게
        } else {
            r = size / 8f;
            canvas.drawRoundRect(new RectF(0, 0, source.getWidth(), source.getHeight()), r, r, paint); // 네모
        }
        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "rounded_corner";
    }
}