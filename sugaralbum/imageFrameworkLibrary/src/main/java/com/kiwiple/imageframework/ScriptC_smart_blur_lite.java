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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/smart_blur_lite.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_smart_blur_lite extends ScriptC {
    private static final String __rs_resource_name = "smart_blur_lite";
    // Constructor
    public  ScriptC_smart_blur_lite(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_smart_blur_lite(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __I32 = Element.I32(rs);
        __F32 = Element.F32(rs);
        __F32_2 = Element.F32_2(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __F32;
    private Element __F32_2;
    private Element __I32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_F32_2;
    private FieldPacker __rs_fp_I32;
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

    private final static int mExportVarIdx_width = 1;
    private int mExportVar_width;
    public synchronized void set_width(int v) {
        setVar(mExportVarIdx_width, v);
        mExportVar_width = v;
    }

    public int get_width() {
        return mExportVar_width;
    }

    public Script.FieldID getFieldID_width() {
        return createFieldID(mExportVarIdx_width, null);
    }

    private final static int mExportVarIdx_height = 2;
    private int mExportVar_height;
    public synchronized void set_height(int v) {
        setVar(mExportVarIdx_height, v);
        mExportVar_height = v;
    }

    public int get_height() {
        return mExportVar_height;
    }

    public Script.FieldID getFieldID_height() {
        return createFieldID(mExportVarIdx_height, null);
    }

    private final static int mExportVarIdx_threshold = 3;
    private float mExportVar_threshold;
    public synchronized void set_threshold(float v) {
        setVar(mExportVarIdx_threshold, v);
        mExportVar_threshold = v;
    }

    public float get_threshold() {
        return mExportVar_threshold;
    }

    public Script.FieldID getFieldID_threshold() {
        return createFieldID(mExportVarIdx_threshold, null);
    }

    private final static int mExportVarIdx_singleStepOffset = 4;
    private Float2 mExportVar_singleStepOffset;
    public synchronized void set_singleStepOffset(Float2 v) {
        mExportVar_singleStepOffset = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addF32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_singleStepOffset, fp, __F32_2, __dimArr);
    }

    public Float2 get_singleStepOffset() {
        return mExportVar_singleStepOffset;
    }

    public Script.FieldID getFieldID_singleStepOffset() {
        return createFieldID(mExportVarIdx_singleStepOffset, null);
    }

    private final static int mExportVarIdx_blurSize = 5;
    private float mExportVar_blurSize;
    public synchronized void set_blurSize(float v) {
        setVar(mExportVarIdx_blurSize, v);
        mExportVar_blurSize = v;
    }

    public float get_blurSize() {
        return mExportVar_blurSize;
    }

    public Script.FieldID getFieldID_blurSize() {
        return createFieldID(mExportVarIdx_blurSize, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_smart_blur = 1;
    public Script.KernelID getKernelID_smart_blur() {
        return createKernelID(mExportForEachIdx_smart_blur, 1, null, null);
    }

    public void forEach_smart_blur(Allocation ain) {
        forEach_smart_blur(ain, null);
    }

    public void forEach_smart_blur(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_smart_blur, ain, null, null, sc);
    }

}

