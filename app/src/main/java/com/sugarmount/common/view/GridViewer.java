package com.sugarmount.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sugarmount.sugaralbum.model.ImageResData;
import com.sugarmount.sugaralbum.R;

public class GridViewer extends LinearLayout {
    ImageView ivImage;
    CheckBox chkImage;


    public GridViewer(Context context) {
        super(context);

        init(context);
    }

    public GridViewer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_cell,this,true);

        chkImage = findViewById(R.id.chkImage);
        ivImage = findViewById(R.id.ivImage);
    }

    public void setItem(ImageResData singerItem){
        Glide.with(this)
                .load(singerItem.contentUri)
                .thumbnail(0.1f)
                .into(ivImage);

        chkImage.setChecked(singerItem.checked);
    }
}
