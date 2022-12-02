
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_rgb_dilation_radius_five;
import com.kiwiple.imageframework.ScriptC_rgb_dilation_radius_four;
import com.kiwiple.imageframework.ScriptC_rgb_dilation_radius_one;
import com.kiwiple.imageframework.ScriptC_rgb_dilation_radius_three;
import com.kiwiple.imageframework.ScriptC_rgb_dilation_radius_two;
import com.kiwiple.imageframework.filter.live.RsYuv;

public class RGBDilation {
    private ScriptC_rgb_dilation_radius_one mDilationRadiusOne;
    private ScriptC_rgb_dilation_radius_two mDilationRadiusTwo;
    private ScriptC_rgb_dilation_radius_three mDilationRadiusThree;
    private ScriptC_rgb_dilation_radius_four mDilationRadiusFour;
    private ScriptC_rgb_dilation_radius_five mDilationRadiusFive;

    private int width;
    private int height;
    private int index;

    private RenderScript mRs;

    private Allocation mAllocationOut;

    public RGBDilation(RenderScript rs, int width, int height) {
        mRs = rs;
        this.width = width;
        this.height = height;

        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        Type.Builder tb = new Type.Builder(mRs, Element.RGBA_8888(mRs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut = Allocation.createTyped(mRs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);
    }

    public void setValues(float[] params) {
        index = (int)params[0];
        switch(index) {
            case 0:
            case 1:
                mDilationRadiusOne = new ScriptC_rgb_dilation_radius_one(mRs);
                mDilationRadiusOne.set_width(width - 1);
                mDilationRadiusOne.set_height(height - 1);
                break;
            case 2:
                mDilationRadiusTwo = new ScriptC_rgb_dilation_radius_two(mRs);
                mDilationRadiusTwo.set_width(width - 1);
                mDilationRadiusTwo.set_height(height - 1);
                break;
            case 3:
                mDilationRadiusThree = new ScriptC_rgb_dilation_radius_three(mRs);
                mDilationRadiusThree.set_width(width - 1);
                mDilationRadiusThree.set_height(height - 1);
                break;
            case 4:
                mDilationRadiusFour = new ScriptC_rgb_dilation_radius_four(mRs);
                mDilationRadiusFour.set_width(width - 1);
                mDilationRadiusFour.set_height(height - 1);
                break;
            case 5:
                mDilationRadiusFive = new ScriptC_rgb_dilation_radius_five(mRs);
                mDilationRadiusFive.set_width(width - 1);
                mDilationRadiusFive.set_height(height - 1);
                break;
            default:
                mDilationRadiusFour = new ScriptC_rgb_dilation_radius_four(mRs);
                mDilationRadiusFour.set_width(width - 1);
                mDilationRadiusFour.set_height(height - 1);
                break;
        }

    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mAllocationOut.copyFrom(allocation);

        switch(index) {
            case 0:
            case 1:
                mDilationRadiusOne.set_inAllocation(mAllocationSub);
                mDilationRadiusOne.set_vertical(true);
                mDilationRadiusOne.forEach_dilation_radius_one(mAllocationOut);

                mDilationRadiusOne.set_inAllocation(mAllocationOut);
                mDilationRadiusOne.set_vertical(false);
                mDilationRadiusOne.forEach_dilation_radius_one(allocation);
                break;
            case 2:
                mDilationRadiusTwo.set_inAllocation(mAllocationSub);
                mDilationRadiusTwo.set_vertical(true);
                mDilationRadiusTwo.forEach_dilation_radius_two(mAllocationOut);

                mDilationRadiusTwo.set_inAllocation(mAllocationOut);
                mDilationRadiusTwo.set_vertical(false);
                mDilationRadiusTwo.forEach_dilation_radius_two(allocation);
                break;
            case 3:
                mDilationRadiusThree.set_inAllocation(mAllocationSub);
                mDilationRadiusThree.set_vertical(true);
                mDilationRadiusThree.forEach_dilation_radius_three(mAllocationOut);

                mDilationRadiusThree.set_inAllocation(mAllocationOut);
                mDilationRadiusThree.set_vertical(false);
                mDilationRadiusThree.forEach_dilation_radius_three(allocation);
                break;
            case 4:
                mDilationRadiusFour.set_inAllocation(mAllocationSub);
                mDilationRadiusFour.set_vertical(true);
                mDilationRadiusFour.forEach_dilation_radius_four(mAllocationOut);

                mDilationRadiusFour.set_inAllocation(mAllocationOut);
                mDilationRadiusFour.set_vertical(false);
                mDilationRadiusFour.forEach_dilation_radius_four(allocation);
                break;
            case 5:
                mDilationRadiusFive.set_inAllocation(mAllocationSub);
                mDilationRadiusFive.set_vertical(true);
                mDilationRadiusFive.forEach_dilation_radius_five(mAllocationOut);

                mDilationRadiusFive.set_inAllocation(mAllocationOut);
                mDilationRadiusFive.set_vertical(false);
                mDilationRadiusFive.forEach_dilation_radius_five(allocation);
                break;
            default:
                mDilationRadiusFour.set_inAllocation(mAllocationSub);
                mDilationRadiusFour.set_vertical(true);
                mDilationRadiusFour.forEach_dilation_radius_four(mAllocationOut);

                mDilationRadiusFour.set_inAllocation(mAllocationOut);
                mDilationRadiusFour.set_vertical(false);
                mDilationRadiusFour.forEach_dilation_radius_four(allocation);
                break;
        }
    }
}
