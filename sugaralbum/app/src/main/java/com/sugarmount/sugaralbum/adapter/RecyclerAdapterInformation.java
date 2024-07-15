package com.sugarmount.sugaralbum.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.sugarmount.common.listener.DataClickEventListener;
import com.sugarmount.common.env.MvConfig;
import com.sugarmount.common.room.info.InfoT;
import com.sugarmount.common.utils.Utils;
import com.sugarmount.sugaralbum.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class RecyclerAdapterInformation extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MvConfig {

    private final DataClickEventListener dataClickEventListener;
    public List<InfoT> mItemList = new ArrayList<>();
    private PAGE_DIRECTION mFlag;

    public RecyclerAdapterInformation(PAGE_DIRECTION flag, DataClickEventListener dataClickEventListener) {
        this.dataClickEventListener = dataClickEventListener;
        mFlag = flag;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_information, parent, false);
        view.setTag(mFlag);
        return new RecyclerItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder viewHolder, final int position) {
        final InfoT item = mItemList.get(position);
        final RecyclerItemViewHolder holder = (RecyclerItemViewHolder) viewHolder;

        //Utils.setAnimation(holder.itemView);

        assert item != null;
        holder.itemTextView1.setText(item.getTitle());
        holder.itemTextView2.setText(item.getContent());

        if(item.getType().equals(INFO_TYPE.LICENSE.toString())){
            holder.itemTextView2.setTextSize(12L);
            holder.itemTextView3.setText(item.getUrl());
            holder.itemTextView4.setVisibility(View.GONE);
        }else{
            holder.itemTextView4.setText(Utils.switchTypeFromString(item.getTime()));
        }

        holder.relativeLayout1.setOnClickListener(view -> {
            view.setEnabled(false);
            boolean open = holder.imageView1.getRotation() < 0L;
            holder.itemTextView2.setVisibility(open ? View.VISIBLE : View.GONE);
            holder.imageView1.setRotation(open ? 90 : -90);
            view.setEnabled(true);
        });

        holder.imageView1.setOnClickListener(view -> holder.relativeLayout1.callOnClick());

        holder.itemTextView1.setOnClickListener(view -> holder.relativeLayout1.callOnClick());

    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

}
