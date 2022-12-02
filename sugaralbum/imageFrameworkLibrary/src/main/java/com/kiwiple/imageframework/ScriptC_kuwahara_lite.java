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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/kuwahara_lite.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_kuwahara_lite extends ScriptC {
    private static final String __rs_resource_name = "kuwahara_lite";
    // Constructor
    public  ScriptC_kuwahara_lite(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_kuwahara_lite(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __I32_2 = Element.I32_2(rs);
        __U32 = Element.U32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __I32_2;
    private Element __U32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_I32_2;
    private FieldPacker __rs_fp_U32;
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

    private final static int mExportVarIdx_negativeFactor = 1;
    private Int2 mExportVar_negativeFactor;
    public synchronized void set_negativeFactor(Int2 v) {
        mExportVar_negativeFactor = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addI32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_negativeFactor, fp, __I32_2, __dimArr);
    }

    public Int2 get_negativeFactor() {
        return mExportVar_negativeFactor;
    }

    public Script.FieldID getFieldID_negativeFactor() {
        return createFieldID(mExportVarIdx_negativeFactor, null);
    }

    private final static int mExportVarIdx_positiveFactor = 2;
    private Int2 mExportVar_positiveFactor;
    public synchronized void set_positiveFactor(Int2 v) {
        mExportVar_positiveFactor = v;
        FieldPacker fp = new FieldPacker(8);
        fp.addI32(v);
        int []__dimArr = new int[1];
        __dimArr[0] = 4;
        setVar(mExportVarIdx_positiveFactor, fp, __I32_2, __dimArr);
    }

    public Int2 get_positiveFactor() {
        return mExportVar_positiveFactor;
    }

    public Script.FieldID getFieldID_positiveFactor() {
        return createFieldID(mExportVarIdx_positiveFactor, null);
    }

    private final static int mExportVarIdx_width = 3;
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

    private final static int mExportVarIdx_height = 4;
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

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_kuwahara = 1;
    public Script.KernelID getKernelID_kuwahara() {
        return createKernelID(mExportForEachIdx_kuwahara, 1, null, null);
    }

    public void forEach_kuwahara(Allocation ain) {
        forEach_kuwahara(ain, null);
    }

    public void forEach_kuwahara(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_kuwahara, ain, null, null, sc);
    }

}

