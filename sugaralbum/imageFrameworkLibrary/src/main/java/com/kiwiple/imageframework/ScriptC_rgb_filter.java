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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/rgb_filter.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_rgb_filter extends ScriptC {
    private static final String __rs_resource_name = "rgb_filter";
    // Constructor
    public  ScriptC_rgb_filter(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_rgb_filter(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __U16 = Element.U16(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __U16;
    private Element __U8_4;
    private FieldPacker __rs_fp_U16;
    private final static int mExportVarIdx_allCurves = 0;
    private int[] mExportVar_allCurves;
    public synchronized void set_allCurves(int[] v) {
        mExportVar_allCurves = v;
        FieldPacker fp = new FieldPacker(512);
        for (int ct1 = 0; ct1 < 256; ct1++) {
            fp.addU16(v[ct1]);
        }

        int []__dimArr = new int[1];
        __dimArr[0] = 256;
        setVar(mExportVarIdx_allCurves, fp, __U16, __dimArr);
    }

    public int[] get_allCurves() {
        return mExportVar_allCurves;
    }

    public Script.FieldID getFieldID_allCurves() {
        return createFieldID(mExportVarIdx_allCurves, null);
    }

    private final static int mExportVarIdx_rCurves = 1;
    private int[] mExportVar_rCurves;
    public synchronized void set_rCurves(int[] v) {
        mExportVar_rCurves = v;
        FieldPacker fp = new FieldPacker(512);
        for (int ct1 = 0; ct1 < 256; ct1++) {
            fp.addU16(v[ct1]);
        }

        int []__dimArr = new int[1];
        __dimArr[0] = 256;
        setVar(mExportVarIdx_rCurves, fp, __U16, __dimArr);
    }

    public int[] get_rCurves() {
        return mExportVar_rCurves;
    }

    public Script.FieldID getFieldID_rCurves() {
        return createFieldID(mExportVarIdx_rCurves, null);
    }

    private final static int mExportVarIdx_gCurves = 2;
    private int[] mExportVar_gCurves;
    public synchronized void set_gCurves(int[] v) {
        mExportVar_gCurves = v;
        FieldPacker fp = new FieldPacker(512);
        for (int ct1 = 0; ct1 < 256; ct1++) {
            fp.addU16(v[ct1]);
        }

        int []__dimArr = new int[1];
        __dimArr[0] = 256;
        setVar(mExportVarIdx_gCurves, fp, __U16, __dimArr);
    }

    public int[] get_gCurves() {
        return mExportVar_gCurves;
    }

    public Script.FieldID getFieldID_gCurves() {
        return createFieldID(mExportVarIdx_gCurves, null);
    }

    private final static int mExportVarIdx_bCurves = 3;
    private int[] mExportVar_bCurves;
    public synchronized void set_bCurves(int[] v) {
        mExportVar_bCurves = v;
        FieldPacker fp = new FieldPacker(512);
        for (int ct1 = 0; ct1 < 256; ct1++) {
            fp.addU16(v[ct1]);
        }

        int []__dimArr = new int[1];
        __dimArr[0] = 256;
        setVar(mExportVarIdx_bCurves, fp, __U16, __dimArr);
    }

    public int[] get_bCurves() {
        return mExportVar_bCurves;
    }

    public Script.FieldID getFieldID_bCurves() {
        return createFieldID(mExportVarIdx_bCurves, null);
    }

    private final static int mExportVarIdx_colorMat = 4;
    private Matrix3f mExportVar_colorMat;
    public synchronized void set_colorMat(Matrix3f v) {
        mExportVar_colorMat = v;
        FieldPacker fp = new FieldPacker(36);
        fp.addMatrix(v);
        setVar(mExportVarIdx_colorMat, fp);
    }

    public Matrix3f get_colorMat() {
        return mExportVar_colorMat;
    }

    public Script.FieldID getFieldID_colorMat() {
        return createFieldID(mExportVarIdx_colorMat, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_rgbFilter = 1;
    public Script.KernelID getKernelID_rgbFilter() {
        return createKernelID(mExportForEachIdx_rgbFilter, 3, null, null);
    }

    public void forEach_rgbFilter(Allocation ain, Allocation aout) {
        forEach_rgbFilter(ain, aout, null);
    }

    public void forEach_rgbFilter(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // Verify dimensions
        Type tIn = ain.getType();
        Type tOut = aout.getType();
        if ((tIn.getCount() != tOut.getCount()) ||
            (tIn.getX() != tOut.getX()) ||
            (tIn.getY() != tOut.getY()) ||
            (tIn.getZ() != tOut.getZ()) ||
            (tIn.hasFaces() != tOut.hasFaces()) ||
            (tIn.hasMipmaps() != tOut.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between input and output parameters!");
        }
        forEach(mExportForEachIdx_rgbFilter, ain, aout, null, sc);
    }

    private final static int mExportFuncIdx_setGreyscale = 0;
    public void invoke_setGreyscale(boolean v) {
        FieldPacker setGreyscale_fp = new FieldPacker(1);
        setGreyscale_fp.addBoolean(v);
        invoke(mExportFuncIdx_setGreyscale, setGreyscale_fp);
    }

    private final static int mExportFuncIdx_setCurve = 1;
    public void invoke_setCurve(boolean v) {
        FieldPacker setCurve_fp = new FieldPacker(1);
        setCurve_fp.addBoolean(v);
        invoke(mExportFuncIdx_setCurve, setCurve_fp);
    }

    private final static int mExportFuncIdx_setSaturation = 2;
    public void invoke_setSaturation(float v) {
        FieldPacker setSaturation_fp = new FieldPacker(4);
        setSaturation_fp.addF32(v);
        invoke(mExportFuncIdx_setSaturation, setSaturation_fp);
    }

    private final static int mExportFuncIdx_setBrightness = 3;
    public void invoke_setBrightness(float v) {
        FieldPacker setBrightness_fp = new FieldPacker(4);
        setBrightness_fp.addF32(v);
        invoke(mExportFuncIdx_setBrightness, setBrightness_fp);
    }

    private final static int mExportFuncIdx_setContrast = 4;
    public void invoke_setContrast(float v) {
        FieldPacker setContrast_fp = new FieldPacker(4);
        setContrast_fp.addF32(v);
        invoke(mExportFuncIdx_setContrast, setContrast_fp);
    }

}

