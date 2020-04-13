package com.sugarmount.sugaralbum.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sugarmount.sugaralbum.R;

public class SingerViewer extends LinearLayout {

    TextView textView;
    TextView textView2;
    ImageView imageView;
    public SingerViewer(Context context) {
        super(context);

        init(context);
    }

    public SingerViewer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.singeritem,this,true);

        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void setItem(SingerItem singerItem){
        textView.setText(singerItem.getName());
        textView2.setText(singerItem.getTel());
        imageView.setImageResource(singerItem.getImage());
    }
}
