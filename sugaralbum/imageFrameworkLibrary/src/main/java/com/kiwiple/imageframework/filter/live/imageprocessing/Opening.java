
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;

public class Opening extends ImageFilterGroup {
    private Erosion erosionFilter;
    private Dilation dilationFilter;

    private Allocation mAllocationOut;

    public Opening(RenderScript rs, int width, int height) {
        erosionFilter = new Erosion(rs, width, height);
        dilationFilter = new Dilation(rs, width, height);

        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);
    }

    public void setValues(float[] params) {
        erosionFilter.setValues(params);
        dilationFilter.setValues(params);
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mAllocationOut.copyFrom(allocation);

        erosionFilter.excute(mAllocationOut, mAllocationSub);

        dilationFilter.excute(allocation, mAllocationOut);
    }
}
