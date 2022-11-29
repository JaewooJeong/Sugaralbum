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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/levels.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_levels extends ScriptC {
    private static final String __rs_resource_name = "levels";
    // Constructor
    public  ScriptC_levels(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_levels(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __F32;
    private Element __U8_4;
    private FieldPacker __rs_fp_F32;
    private final static int mExportVarIdx_inBlack = 0;
    private float mExportVar_inBlack;
    public synchronized void set_inBlack(float v) {
        setVar(mExportVarIdx_inBlack, v);
        mExportVar_inBlack = v;
    }

    public float get_inBlack() {
        return mExportVar_inBlack;
    }

    public Script.FieldID getFieldID_inBlack() {
        return createFieldID(mExportVarIdx_inBlack, null);
    }

    private final static int mExportVarIdx_overInWMinInB = 1;
    private float mExportVar_overInWMinInB;
    public synchronized void set_overInWMinInB(float v) {
        setVar(mExportVarIdx_overInWMinInB, v);
        mExportVar_overInWMinInB = v;
    }

    public float get_overInWMinInB() {
        return mExportVar_overInWMinInB;
    }

    public Script.FieldID getFieldID_overInWMinInB() {
        return createFieldID(mExportVarIdx_overInWMinInB, null);
    }

    private final static int mExportVarIdx_gamma = 2;
    private float mExportVar_gamma;
    public synchronized void set_gamma(float v) {
        setVar(mExportVarIdx_gamma, v);
        mExportVar_gamma = v;
    }

    public float get_gamma() {
        return mExportVar_gamma;
    }

    public Script.FieldID getFieldID_gamma() {
        return createFieldID(mExportVarIdx_gamma, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_levels = 1;
    public Script.KernelID getKernelID_levels() {
        return createKernelID(mExportForEachIdx_levels, 1, null, null);
    }

    public void forEach_levels(Allocation ain) {
        forEach_levels(ain, null);
    }

    public void forEach_levels(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_levels, ain, null, null, sc);
    }

}

