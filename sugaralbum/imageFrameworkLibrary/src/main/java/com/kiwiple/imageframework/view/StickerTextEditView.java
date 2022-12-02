
package com.kiwiple.imageframework.view;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * 텍스트 스티커의 내용을 편집하는 뷰
 */
public class StickerTextEditView extends RelativeLayout {
    private EditText mEditText;

    private boolean mCurrentFrameModify;

    public StickerTextEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public StickerTextEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerTextEditView(Context context) {
        super(context);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        mEditText = new EditText(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                                                             ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                             ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.leftMargin = (int)(30 * density);
        params.rightMargin = (int)(30 * density);
        mEditText.setLayoutParams(params);
        mEditText.setBackgroundColor(Color.TRANSPARENT);
        mEditText.setMinWidth((int)(100 * density));
        mEditText.setGravity(Gravity.CENTER);
        mEditText.setInputType(mEditText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEditText.setTextSize(15 * density);
        mEditText.setHorizontallyScrolling(false);
        addView(mEditText);
    }

    /**
     * 현재 프레임의 수정 여부 설정
     * 
     * @param currentFrameModify 프레임의 수정 여부
     */
    public void setTextStickerEdit(boolean currentFrameModify) {
        mCurrentFrameModify = currentFrameModify;
    }

    /**
     * 프레임의 수정 여부 반환
     * 
     * @return 프레임의 수정 여부
     */
    public boolean isTextStickerEdit() {
        return mCurrentFrameModify;
    }

    public EditText getEditText() {
        return mEditText;
    }
}
