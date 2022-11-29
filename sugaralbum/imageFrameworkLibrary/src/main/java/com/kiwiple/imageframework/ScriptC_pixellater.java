/*
 * Copyright (C) 2011-2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/pixellater.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_pixellater extends ScriptC {
    private static final String __rs_resource_name = "pixellater";
    // Constructor
    public  ScriptC_pixellater(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_pixellater(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __F32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private final static int mExportVarIdx_inAllocation = 0;
    private Allocation mExportVar_inAllocation;
    public synchronized void set_inAllocation(Allocation v) {
        setVar(mExportVarIdx_inAllocation, v);
        mExportVar_inAllocation = v;
    }

    public Allocation get_inAllocation() {
        return mExportVar_inAllocation;
    }

    public Script.FieldID getFieldID_inAllocation() {
        return createFieldID(mExportVarIdx_inAllocation, null);
    }

    private final static int mExportVarIdx_fractionalWidthOfPixel = 1;
    private float mExportVar_fractionalWidthOfPixel;
    public synchronized void set_fractionalWidthOfPixel(float v) {
        setVar(mExportVarIdx_fractionalWidthOfPixel, v);
        mExportVar_fractionalWidthOfPixel = v;
    }

    public float get_fractionalWidthOfPixel() {
        return mExportVar_fractionalWidthOfPixel;
    }

    public Script.FieldID getFieldID_fractionalWidthOfPixel() {
        return createFieldID(mExportVarIdx_fractionalWidthOfPixel, null);
    }

    private final static int mExportVarIdx_aspectRatio = 2;
    private float mExportVar_aspectRatio;
    public synchronized void set_aspectRatio(float v) {
        setVar(mExportVarIdx_aspectRatio, v);
        mExportVar_aspectRatio = v;
    }

    public float get_aspectRatio() {
        return mExportVar_aspectRatio;
    }

    public Script.FieldID getFieldID_aspectRatio() {
        return createFieldID(mExportVarIdx_aspectRatio, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_pixellater = 1;
    public Script.KernelID getKernelID_pixellater() {
        return createKernelID(mExportForEachIdx_pixellater, 1, null, null);
    }

    public void forEach_pixellater(Allocation ain) {
        forEach_pixellater(ain, null);
    }

    public void forEach_pixellater(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_pixellater, ain, null, null, sc);
    }

}

