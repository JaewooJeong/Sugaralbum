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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/pinch_distortion.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_pinch_distortion extends ScriptC {
    private static final String __rs_resource_name = "pinch_distortion";
    // Constructor
    public  ScriptC_pinch_distortion(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_pinch_distortion(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __F32_2 = Element.F32_2(rs);
        __F32 = Element.F32(rs);
        __I32 = Element.I32(rs);
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

    private final static int mExportVarIdx_center = 1;
    private Float2 mExportVar_center;
    public synchronized void set_center(Float2 v) {
        mExportVar_center = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addF32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_center, fp, __F32_2, __dimArr);
    }

    public Float2 get_center() {
        return mExportVar_center;
    }

    public Script.FieldID getFieldID_center() {
        return createFieldID(mExportVarIdx_center, null);
    }

    private final static int mExportVarIdx_radius = 2;
    private float mExportVar_radius;
    public synchronized void set_radius(float v) {
        setVar(mExportVarIdx_radius, v);
        mExportVar_radius = v;
    }

    public float get_radius() {
        return mExportVar_radius;
    }

    public Script.FieldID getFieldID_radius() {
        return createFieldID(mExportVarIdx_radius, null);
    }

    private final static int mExportVarIdx_scale = 3;
    private float mExportVar_scale;
    public synchronized void set_scale(float v) {
        setVar(mExportVarIdx_scale, v);
        mExportVar_scale = v;
    }

    public float get_scale() {
        return mExportVar_scale;
    }

    public Script.FieldID getFieldID_scale() {
        return createFieldID(mExportVarIdx_scale, null);
    }

    private final static int mExportVarIdx_width = 4;
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

    private final static int mExportVarIdx_height = 5;
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

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_pinch_distortion = 1;
    public Script.KernelID getKernelID_pinch_distortion() {
        return createKernelID(mExportForEachIdx_pinch_distortion, 1, null, null);
    }

    public void forEach_pinch_distortion(Allocation ain) {
        forEach_pinch_distortion(ain, null);
    }

    public void forEach_pinch_distortion(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_pinch_distortion, ain, null, null, sc);
    }

}

