package com.sugarmount.sugarcamera.story.gallery;



import com.sugarmount.sugarcamera.story.utils.DateUtil;
import com.sugarmount.sugaralbum.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class RowItemView extends FrameLayout implements OnClickListener, OnLongClickListener {
	
	private static final String TAG = "RowItemView";
	
	private ItemMediaData mMediaData;
	
	// list position data
	private int mPosition = -1;
	private int mGroupPosition = -1;
	
	// save Properties
	private Rect mRect;
	private PointF mPointF;
	
	private ThumbnailView mThumbnailView;
	private ImageView mIvTypeIcon;
	private TextView mTvVideoDuration;
	private View mClickView;
	private CheckBox mCbChecked;
	
	private OnClickListener mOnClickListener;
	private OnLongClickListener mOnLongClickListener;
	
	private OnRowItemCheckedClickListener mOnRowItemCheckedClickListener;
	
	private View mLayout; 
	
	public interface OnRowItemCheckedClickListener{
		public void OnDataChecked(View view, boolean isChecked); 
	}

	public RowItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public RowItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public RowItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	private void init(Context context) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		mLayout = inflater.inflate(R.layout.activity_story_gallery_list_child_item, this);
		
		mThumbnailView = (ThumbnailView)mLayout.findViewById(R.id.kiwiple_story_list_child_item_thumbnail);
		mThumbnailView.setRowItemView(this);
		mIvTypeIcon = (ImageView)mLayout.findViewById(R.id.kiwiple_story_list_child_item_type_img);
		mTvVideoDuration = (TextView)mLayout.findViewById(R.id.kiwiple_story_list_child_item_videoDuration_tv);
		mClickView = (View)mLayout.findViewById(R.id.kiwiple_story_list_child_item_videoClick_img);
		mClickView.setOnClickListener(this);
		mClickView.setOnLongClickListener(this);
		mCbChecked = (CheckBox)mLayout.findViewById(R.id.kiwiple_story_list_child_item_checkBox);
	}
	
	public void setCheckBoxClickEnable(boolean bClick){
		mCbChecked.setClickable(bClick); 
	}
	
	public void setOnCheckBoxChangeListener(OnRowItemCheckedClickListener listener){
		mOnRowItemCheckedClickListener = listener; 
	}
	
	public CheckBox getCheckBoxView(){
		return mCbChecked; 
	}
	
	@Override
	public void setOnClickListener(OnClickListener l) {
		// TODO Auto-generated method stub
		mOnClickListener = l;
	}
	
	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		// TODO Auto-generated method stub
		mOnLongClickListener = l;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(mOnClickListener != null ) {
			mOnClickListener.onClick(this);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		if(mOnLongClickListener != null) {
			return mOnLongClickListener.onLongClick(this);
		}
		return false;
	}
	
	public ThumbnailView getThumbnailView() {
		return mThumbnailView;
	}
	
	public void setPosition(int groupPostion, int childPosition) {
		mPosition = childPosition;
		mGroupPosition = groupPostion;
	}
	
	public int getPosition() {
		return mPosition;
	}
	
	public int getGroupPosition() {
	    return mGroupPosition;
	}
	
	public void setMediaData(ItemMediaData data) {
		mMediaData = data;
	}
	
	public long getContentId() {
		return mMediaData._id;
	}
	
	public String getContentPath() {
		return mMediaData.contentPath;
	}
	
	public boolean isVideo() {
		return mMediaData.isVideo;
	}
	
	public boolean invalid() {
		return mMediaData.invalid;
	}
	
	public void displayVideoUI(int visibility) {
		if(isVideo() && visibility == View.VISIBLE) {
			mTvVideoDuration.setText(DateUtil.getDuration(mMediaData.duration));
			mTvVideoDuration.setVisibility(View.VISIBLE);
			mIvTypeIcon.setImageResource(R.drawable.btn_gallery_play_2_nor);
			mIvTypeIcon.setVisibility(View.VISIBLE);
		} else {
			mTvVideoDuration.setVisibility(View.GONE);
			mIvTypeIcon.setVisibility(View.GONE);
		}
	}
	
	public void displayInvalidThumbnail() {
		mIvTypeIcon.setImageResource(isVideo() ? R.drawable.img_gallery_default_video_2x2 : R.drawable.img_gallery_default_photo_2x2);
		mIvTypeIcon.setVisibility(View.VISIBLE);
	}
	
	public void setSelectMode(boolean mode, boolean enabled, boolean checked) {
		if(mode) {
			mCbChecked.setEnabled(enabled);
			mCbChecked.setChecked(enabled & checked);
			mCbChecked.setVisibility(View.VISIBLE);
			mClickView.setBackgroundResource(R.drawable.bg_frame_gallery_nor);
		} else {
			mCbChecked.setVisibility(View.GONE);
			mCbChecked.setChecked(false);
			mClickView.setBackgroundResource(R.drawable.gallery_image_bg_selector);
		}
	}
	
	public void setClickViewBackground(boolean isSelected){
		if(isSelected){
			mClickView.setBackgroundResource(R.drawable.bg_frame_gallery_foc); 
		}else{
			mClickView.setBackgroundResource(R.drawable.bg_frame_gallery_nor);
		}
	}
	
	
	public boolean toggleSelected() {
		boolean checked = !mCbChecked.isChecked();
		mCbChecked.setChecked(checked);
		return checked;
	}
	
	public boolean isSelected(){
		return mCbChecked.isChecked(); 
	}
	
	public boolean isSelectEnabled(){
		return mCbChecked.isEnabled(); 
	}
	
	public void saveProperties() {
		mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
		mPointF = new PointF(getX(), getY());
	}
	
	public void restoreProperties() {
		if(mRect != null) {
			setLeft(mRect.left);
			setTop(mRect.top);
			setRight(mRect.right);
			setBottom(mRect.bottom);
			mRect = null;
		}
		if(mPointF != null) {
			setX(mPointF.x);
			setY(mPointF.y);
			mPointF = null;
		}
	}
	
	public void setImageBitmap(Bitmap bitmap) {
		mThumbnailView.setImageBitmap(bitmap);
	}
	
	public void updateView(int x, int y, int w, int h) {
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
		if(params != null) {
			// location
			setTranslationX(x);
			setTranslationY(y);
			// size
			params.width = w;
			params.height = h;
			setLayoutParams(params);
		}
	}
	
	public void setOnCheckBoxListener(OnClickListener listener){
		mCbChecked.setOnClickListener(listener); 
	}
}