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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/gaussian_lite.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_gaussian_lite extends ScriptC {
    private static final String __rs_resource_name = "gaussian_lite";
    // Constructor
    public  ScriptC_gaussian_lite(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_gaussian_lite(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __U32 = Element.U32(rs);
        __U32_2 = Element.U32_2(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __U32;
    private Element __U32_2;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_U32;
    private FieldPacker __rs_fp_U32_2;
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
    private long mExportVar_width;
    public synchronized void set_width(long v) {
        if (__rs_fp_U32!= null) {
            __rs_fp_U32.reset();
        } else {
            __rs_fp_U32 = new FieldPacker(4);
        }
        __rs_fp_U32.addU32(v);
        setVar(mExportVarIdx_width, __rs_fp_U32);
        mExportVar_width = v;
    }

    public long get_width() {
        return mExportVar_width;
    }

    public Script.FieldID getFieldID_width() {
        return createFieldID(mExportVarIdx_width, null);
    }

    private final static int mExportVarIdx_height = 2;
    private long mExportVar_height;
    public synchronized void set_height(long v) {
        if (__rs_fp_U32!= null) {
            __rs_fp_U32.reset();
        } else {
            __rs_fp_U32 = new FieldPacker(4);
        }
        __rs_fp_U32.addU32(v);
        setVar(mExportVarIdx_height, __rs_fp_U32);
        mExportVar_height = v;
    }

    public long get_height() {
        return mExportVar_height;
    }

    public Script.FieldID getFieldID_height() {
        return createFieldID(mExportVarIdx_height, null);
    }

    private final static int mExportVarIdx_firstStepOffset = 3;
    private Long2 mExportVar_firstStepOffset;
    public synchronized void set_firstStepOffset(Long2 v) {
        mExportVar_firstStepOffset = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addU32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_firstStepOffset, fp, __U32_2, __dimArr);
    }

    public Long2 get_firstStepOffset() {
        return mExportVar_firstStepOffset;
    }

    public Script.FieldID getFieldID_firstStepOffset() {
        return createFieldID(mExportVarIdx_firstStepOffset, null);
    }

    private final static int mExportVarIdx_secondStepOffset = 4;
    private Long2 mExportVar_secondStepOffset;
    public synchronized void set_secondStepOffset(Long2 v) {
        mExportVar_secondStepOffset = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addU32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_secondStepOffset, fp, __U32_2, __dimArr);
    }

    public Long2 get_secondStepOffset() {
        return mExportVar_secondStepOffset;
    }

    public Script.FieldID getFieldID_secondStepOffset() {
        return createFieldID(mExportVarIdx_secondStepOffset, null);
    }

    private final static int mExportVarIdx_thirdStepOffset = 5;
    private Long2 mExportVar_thirdStepOffset;
    public synchronized void set_thirdStepOffset(Long2 v) {
        mExportVar_thirdStepOffset = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addU32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_thirdStepOffset, fp, __U32_2, __dimArr);
    }

    public Long2 get_thirdStepOffset() {
        return mExportVar_thirdStepOffset;
    }

    public Script.FieldID getFieldID_thirdStepOffset() {
        return createFieldID(mExportVarIdx_thirdStepOffset, null);
    }

    private final static int mExportVarIdx_firthStepOffset = 6;
    private Long2 mExportVar_firthStepOffset;
    public synchronized void set_firthStepOffset(Long2 v) {
        mExportVar_firthStepOffset = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addU32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_firthStepOffset, fp, __U32_2, __dimArr);
    }

    public Long2 get_firthStepOffset() {
        return mExportVar_firthStepOffset;
    }

    public Script.FieldID getFieldID_firthStepOffset() {
        return createFieldID(mExportVarIdx_firthStepOffset, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_gaussian = 1;
    public Script.KernelID getKernelID_gaussian() {
        return createKernelID(mExportForEachIdx_gaussian, 1, null, null);
    }

    public void forEach_gaussian(Allocation ain) {
        forEach_gaussian(ain, null);
    }

    public void forEach_gaussian(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_gaussian, ain, null, null, sc);
    }

}

