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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/box_blur_lite.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_box_blur_lite extends ScriptC {
    private static final String __rs_resource_name = "box_blur_lite";
    // Constructor
    public  ScriptC_box_blur_lite(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_box_blur_lite(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __F32 = Element.F32(rs);
        __F32_2 = Element.F32_2(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __F32;
    private Element __F32_2;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_F32_2;
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
    private float mExportVar_width;
    public synchronized void set_width(float v) {
        setVar(mExportVarIdx_width, v);
        mExportVar_width = v;
    }

    public float get_width() {
        return mExportVar_width;
    }

    public Script.FieldID getFieldID_width() {
        return createFieldID(mExportVarIdx_width, null);
    }

    private final static int mExportVarIdx_height = 2;
    private float mExportVar_height;
    public synchronized void set_height(float v) {
        setVar(mExportVarIdx_height, v);
        mExportVar_height = v;
    }

    public float get_height() {
        return mExportVar_height;
    }

    public Script.FieldID getFieldID_height() {
        return createFieldID(mExportVarIdx_height, null);
    }

    private final static int mExportVarIdx_firstOffset = 3;
    private Float2 mExportVar_firstOffset;
    public synchronized void set_firstOffset(Float2 v) {
        mExportVar_firstOffset = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addF32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_firstOffset, fp, __F32_2, __dimArr);
    }

    public Float2 get_firstOffset() {
        return mExportVar_firstOffset;
    }

    public Script.FieldID getFieldID_firstOffset() {
        return createFieldID(mExportVarIdx_firstOffset, null);
    }

    private final static int mExportVarIdx_secondOffset = 4;
    private Float2 mExportVar_secondOffset;
    public synchronized void set_secondOffset(Float2 v) {
        mExportVar_secondOffset = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addF32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_secondOffset, fp, __F32_2, __dimArr);
    }

    public Float2 get_secondOffset() {
        return mExportVar_secondOffset;
    }

    public Script.FieldID getFieldID_secondOffset() {
        return createFieldID(mExportVarIdx_secondOffset, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_boxBlur = 1;
    public Script.KernelID getKernelID_boxBlur() {
        return createKernelID(mExportForEachIdx_boxBlur, 1, null, null);
    }

    public void forEach_boxBlur(Allocation ain) {
        forEach_boxBlur(ain, null);
    }

    public void forEach_boxBlur(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_boxBlur, ain, null, null, sc);
    }

}

