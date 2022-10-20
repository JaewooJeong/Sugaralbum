package com.sugarmount.sugaralbum.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sugarmount.common.model.MvConfig;
import com.sugarmount.sugaralbum.R;


public class RecyclerItemViewHolder extends RecyclerView.ViewHolder implements MvConfig {

    public ImageView imageView1, imageView2, imageView3, imageView4;
    public TextView itemTextView1, itemTextView2, itemTextView3, itemTextView4, itemTextView5, itemTextView6, itemTextView7, itemTextView8, itemTextView9, itemTextView10, itemTextView11;
    public RelativeLayout relativeLayout1, relativeLayout2, relativeLayout3, relativeLayout4, relativeLayout5, relativeLayout6, container;
    public CheckBox checkBox1;
    public RecyclerItemViewHolder(final View parent) {
        super(parent);

        PAGE_DIRECTION rc = (PAGE_DIRECTION) parent.getTag();
        switch (rc){
            case CLIP:
                imageView1      = parent.findViewById(R.id.imageView1);
            case NAV:
                imageView1      = parent.findViewById(R.id.imageView1);
                imageView2      = parent.findViewById(R.id.imageView2);
                itemTextView1   = parent.findViewById(R.id.itemTextView1);
                itemTextView2   = parent.findViewById(R.id.itemTextView2);
                relativeLayout1 = parent.findViewById(R.id.relativeLayout1);
                relativeLayout2 = parent.findViewById(R.id.relativeLayout2);
                relativeLayout3 = parent.findViewById(R.id.relativeLayout3);
                relativeLayout6 = parent.findViewById(R.id.relativeLayout6);
                break;
            case MAIN:
                itemTextView1   = parent.findViewById(R.id.itemTextView1);
                itemTextView2   = parent.findViewById(R.id.itemTextView2);
                itemTextView3   = parent.findViewById(R.id.itemTextView3);
                itemTextView4   = parent.findViewById(R.id.itemTextView4);
                relativeLayout1 = parent.findViewById(R.id.relativeLayout1);
                relativeLayout4 = parent.findViewById(R.id.relativeLayout4);
                break;
            case INFORMATION:
                imageView1      = parent.findViewById(R.id.imageView1);
                itemTextView1   = parent.findViewById(R.id.itemTextView1);
                itemTextView2   = parent.findViewById(R.id.itemTextView2);
                itemTextView3   = parent.findViewById(R.id.itemTextView3);
                itemTextView4   = parent.findViewById(R.id.itemTextView4);
                relativeLayout1 = parent.findViewById(R.id.relativeLayout1);
                break;
        }

    }

}
