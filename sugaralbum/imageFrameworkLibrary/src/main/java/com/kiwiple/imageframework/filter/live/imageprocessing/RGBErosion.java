
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_rgb_erosion_radius_five;
import com.kiwiple.imageframework.ScriptC_rgb_erosion_radius_four;
import com.kiwiple.imageframework.ScriptC_rgb_erosion_radius_one;
import com.kiwiple.imageframework.ScriptC_rgb_erosion_radius_three;
import com.kiwiple.imageframework.ScriptC_rgb_erosion_radius_two;
import com.kiwiple.imageframework.filter.live.RsYuv;

public class RGBErosion {
    private ScriptC_rgb_erosion_radius_one mErosionRadiusOne;
    private ScriptC_rgb_erosion_radius_two mErosionRadiusTwo;
    private ScriptC_rgb_erosion_radius_three mErosionRadiusThree;
    private ScriptC_rgb_erosion_radius_four mErosionRadiusFour;
    private ScriptC_rgb_erosion_radius_five mErosionRadiusFive;

    private int width;
    private int height;
    private int index;

    private RenderScript mRs;

    private Allocation mAllocationOut;

    public RGBErosion(RenderScript rs, int width, int height) {
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
                mErosionRadiusOne = new ScriptC_rgb_erosion_radius_one(mRs);
                mErosionRadiusOne.set_width(width - 1);
                mErosionRadiusOne.set_height(height - 1);
                break;
            case 2:
                mErosionRadiusTwo = new ScriptC_rgb_erosion_radius_two(mRs);
                mErosionRadiusTwo.set_width(width - 1);
                mErosionRadiusTwo.set_height(height - 1);
                break;
            case 3:
                mErosionRadiusThree = new ScriptC_rgb_erosion_radius_three(mRs);
                mErosionRadiusThree.set_width(width - 1);
                mErosionRadiusThree.set_height(height - 1);
                break;
            case 4:
                mErosionRadiusFour = new ScriptC_rgb_erosion_radius_four(mRs);
                mErosionRadiusFour.set_width(width - 1);
                mErosionRadiusFour.set_height(height - 1);
                break;
            case 5:
                mErosionRadiusFive = new ScriptC_rgb_erosion_radius_five(mRs);
                mErosionRadiusFive.set_width(width - 1);
                mErosionRadiusFive.set_height(height - 1);
                break;
            default:
                mErosionRadiusFour = new ScriptC_rgb_erosion_radius_four(mRs);
                mErosionRadiusFour.set_width(width - 1);
                mErosionRadiusFour.set_height(height - 1);
                break;
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mAllocationOut.copyFrom(allocation);
        switch(index) {
            case 0:
            case 1:
                mErosionRadiusOne.set_inAllocation(mAllocationSub);
                mErosionRadiusOne.set_vertical(true);
                mErosionRadiusOne.forEach_erosion_radius_one(mAllocationOut);

                mErosionRadiusOne.set_inAllocation(mAllocationOut);
                mErosionRadiusOne.set_vertical(false);
                mErosionRadiusOne.forEach_erosion_radius_one(allocation);
                break;
            case 2:
                mErosionRadiusTwo.set_inAllocation(mAllocationSub);
                mErosionRadiusTwo.set_vertical(true);
                mErosionRadiusTwo.forEach_erosion_radius_two(mAllocationOut);

                mErosionRadiusTwo.set_inAllocation(mAllocationOut);
                mErosionRadiusTwo.set_vertical(false);
                mErosionRadiusTwo.forEach_erosion_radius_two(allocation);
                break;
            case 3:
                mErosionRadiusThree.set_inAllocation(mAllocationSub);
                mErosionRadiusThree.set_vertical(true);
                mErosionRadiusThree.forEach_erosion_radius_three(mAllocationOut);

                mErosionRadiusThree.set_inAllocation(mAllocationOut);
                mErosionRadiusThree.set_vertical(false);
                mErosionRadiusThree.forEach_erosion_radius_three(allocation);
                break;
            case 4:
                mErosionRadiusFour.set_inAllocation(mAllocationSub);
                mErosionRadiusFour.set_vertical(true);
                mErosionRadiusFour.forEach_erosion_radius_four(mAllocationOut);

                mErosionRadiusFour.set_inAllocation(mAllocationOut);
                mErosionRadiusFour.set_vertical(false);
                mErosionRadiusFour.forEach_erosion_radius_four(allocation);
                break;
            case 5:
                mErosionRadiusFive.set_inAllocation(mAllocationSub);
                mErosionRadiusFive.set_vertical(true);
                mErosionRadiusFive.forEach_erosion_radius_five(mAllocationOut);

                mErosionRadiusFive.set_inAllocation(mAllocationOut);
                mErosionRadiusFive.set_vertical(false);
                mErosionRadiusFive.forEach_erosion_radius_five(allocation);
                break;
            default:
                mErosionRadiusFour.set_inAllocation(mAllocationSub);
                mErosionRadiusFour.set_vertical(true);
                mErosionRadiusFour.forEach_erosion_radius_four(mAllocationOut);

                mErosionRadiusFour.set_inAllocation(mAllocationOut);
                mErosionRadiusFour.set_vertical(false);
                mErosionRadiusFour.forEach_erosion_radius_four(allocation);
                break;
        }
    }
}
