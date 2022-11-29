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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/hue.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_hue extends ScriptC {
    private static final String __rs_resource_name = "hue";
    // Constructor
    public  ScriptC_hue(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_hue(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __F32;
    private Element __U8_4;
    private FieldPacker __rs_fp_F32;
    private final static int mExportVarIdx_hueAdjust = 0;
    private float mExportVar_hueAdjust;
    public synchronized void set_hueAdjust(float v) {
        setVar(mExportVarIdx_hueAdjust, v);
        mExportVar_hueAdjust = v;
    }

    public float get_hueAdjust() {
        return mExportVar_hueAdjust;
    }

    public Script.FieldID getFieldID_hueAdjust() {
        return createFieldID(mExportVarIdx_hueAdjust, null);
    }

    private final static int mExportVarIdx_rgbToYiq = 1;
    private Matrix3f mExportVar_rgbToYiq;
    public synchronized void set_rgbToYiq(Matrix3f v) {
        mExportVar_rgbToYiq = v;
        FieldPacker fp = new FieldPacker(36);
        fp.addMatrix(v);
        setVar(mExportVarIdx_rgbToYiq, fp);
    }

    public Matrix3f get_rgbToYiq() {
        return mExportVar_rgbToYiq;
    }

    public Script.FieldID getFieldID_rgbToYiq() {
        return createFieldID(mExportVarIdx_rgbToYiq, null);
    }

    private final static int mExportVarIdx_YiqToRgb = 2;
    private Matrix3f mExportVar_YiqToRgb;
    public synchronized void set_YiqToRgb(Matrix3f v) {
        mExportVar_YiqToRgb = v;
        FieldPacker fp = new FieldPacker(36);
        fp.addMatrix(v);
        setVar(mExportVarIdx_YiqToRgb, fp);
    }

    public Matrix3f get_YiqToRgb() {
        return mExportVar_YiqToRgb;
    }

    public Script.FieldID getFieldID_YiqToRgb() {
        return createFieldID(mExportVarIdx_YiqToRgb, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_hue = 1;
    public Script.KernelID getKernelID_hue() {
        return createKernelID(mExportForEachIdx_hue, 1, null, null);
    }

    public void forEach_hue(Allocation ain) {
        forEach_hue(ain, null);
    }

    public void forEach_hue(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_hue, ain, null, null, sc);
    }

}

