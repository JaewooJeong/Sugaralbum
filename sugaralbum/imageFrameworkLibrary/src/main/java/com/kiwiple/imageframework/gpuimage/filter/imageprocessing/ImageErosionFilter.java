
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoPassTextureSamplingFilter;

public class ImageErosionFilter extends ImageTwoPassTextureSamplingFilter {
    private ArtFilterInfo mArtFilterInfo;

    public void initWithRadius(Context context, int dilationRadius) {
        String fragmentShaderForThisRadius;
        String vertexShaderForThisRadius;

        switch(dilationRadius) {
            case 0:
            case 1: {
                vertexShaderForThisRadius = "dilation_radius_one_vertex";
                fragmentShaderForThisRadius = "erosion_radius_one_fragment";
            }
                break;
            case 2: {
                vertexShaderForThisRadius = "dilation_radius_two_vertex";
                fragmentShaderForThisRadius = "erosion_radius_two_fragment";
            }
                break;
            case 3: {
                vertexShaderForThisRadius = "dilation_radius_three_vertex";
                fragmentShaderForThisRadius = "erosion_radius_three_fragment";
            }
                break;
            case 4: {
                vertexShaderForThisRadius = "dilation_radius_four_vertex";
                fragmentShaderForThisRadius = "erosion_radius_four_fragment";
            }
                break;
            case 5: {
                vertexShaderForThisRadius = "dilation_radius_five_vertex";
                fragmentShaderForThisRadius = "erosion_radius_five_fragment";
            }
                break;
            default: {
                vertexShaderForThisRadius = "dilation_radius_four_vertex";
                fragmentShaderForThisRadius = "erosion_radius_four_fragment";
            }
                break;
        }

        super.initWithFirstStageVertexShaderFromResource(context, vertexShaderForThisRadius,
                                                         fragmentShaderForThisRadius,
                                                         vertexShaderForThisRadius,
                                                         fragmentShaderForThisRadius);
    }

    @Override
    public void init(Context context) {
        this.initWithRadius(context, 1);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(5.f, 1.f, 3.f, 1.f, "Radius"));
            mArtFilterInfo = new ArtFilterInfo("Erosion", progressInfo);
        }
        return mArtFilterInfo;
    }
}
