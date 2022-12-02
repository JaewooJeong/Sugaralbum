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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/unsharp_mask.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_unsharp_mask extends ScriptC {
    private static final String __rs_resource_name = "unsharp_mask";
    // Constructor
    public  ScriptC_unsharp_mask(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_unsharp_mask(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __SAMPLER = Element.SAMPLER(rs);
        __I32 = Element.I32(rs);
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __F32;
    private Element __I32;
    private Element __SAMPLER;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_I32;
    private FieldPacker __rs_fp_SAMPLER;
    private final static int mExportVarIdx_inAllocation1 = 0;
    private Allocation mExportVar_inAllocation1;
    public synchronized void set_inAllocation1(Allocation v) {
        setVar(mExportVarIdx_inAllocation1, v);
        mExportVar_inAllocation1 = v;
    }

    public Allocation get_inAllocation1() {
        return mExportVar_inAllocation1;
    }

    public Script.FieldID getFieldID_inAllocation1() {
        return createFieldID(mExportVarIdx_inAllocation1, null);
    }

    private final static int mExportVarIdx_inAllocation2 = 1;
    private Allocation mExportVar_inAllocation2;
    public synchronized void set_inAllocation2(Allocation v) {
        setVar(mExportVarIdx_inAllocation2, v);
        mExportVar_inAllocation2 = v;
    }

    public Allocation get_inAllocation2() {
        return mExportVar_inAllocation2;
    }

    public Script.FieldID getFieldID_inAllocation2() {
        return createFieldID(mExportVarIdx_inAllocation2, null);
    }

    private final static int mExportVarIdx_sampler = 2;
    private Sampler mExportVar_sampler;
    public synchronized void set_sampler(Sampler v) {
        setVar(mExportVarIdx_sampler, v);
        mExportVar_sampler = v;
    }

    public Sampler get_sampler() {
        return mExportVar_sampler;
    }

    public Script.FieldID getFieldID_sampler() {
        return createFieldID(mExportVarIdx_sampler, null);
    }

    private final static int mExportVarIdx_width = 3;
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

    private final static int mExportVarIdx_height = 4;
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

    private final static int mExportVarIdx_intensity = 5;
    private float mExportVar_intensity;
    public synchronized void set_intensity(float v) {
        setVar(mExportVarIdx_intensity, v);
        mExportVar_intensity = v;
    }

    public float get_intensity() {
        return mExportVar_intensity;
    }

    public Script.FieldID getFieldID_intensity() {
        return createFieldID(mExportVarIdx_intensity, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_unsharp_mask = 1;
    public Script.KernelID getKernelID_unsharp_mask() {
        return createKernelID(mExportForEachIdx_unsharp_mask, 1, null, null);
    }

    public void forEach_unsharp_mask(Allocation ain) {
        forEach_unsharp_mask(ain, null);
    }

    public void forEach_unsharp_mask(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_unsharp_mask, ain, null, null, sc);
    }

}

