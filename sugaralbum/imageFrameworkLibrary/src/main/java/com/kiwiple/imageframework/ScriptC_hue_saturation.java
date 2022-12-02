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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/hue_saturation.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_hue_saturation extends ScriptC {
    private static final String __rs_resource_name = "hue_saturation";
    // Constructor
    public  ScriptC_hue_saturation(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_hue_saturation(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __F32;
    private Element __U8_4;
    private FieldPacker __rs_fp_F32;
    private final static int mExportVarIdx_redSaturation = 0;
    private float mExportVar_redSaturation;
    public synchronized void set_redSaturation(float v) {
        setVar(mExportVarIdx_redSaturation, v);
        mExportVar_redSaturation = v;
    }

    public float get_redSaturation() {
        return mExportVar_redSaturation;
    }

    public Script.FieldID getFieldID_redSaturation() {
        return createFieldID(mExportVarIdx_redSaturation, null);
    }

    private final static int mExportVarIdx_yellowSaturation = 1;
    private float mExportVar_yellowSaturation;
    public synchronized void set_yellowSaturation(float v) {
        setVar(mExportVarIdx_yellowSaturation, v);
        mExportVar_yellowSaturation = v;
    }

    public float get_yellowSaturation() {
        return mExportVar_yellowSaturation;
    }

    public Script.FieldID getFieldID_yellowSaturation() {
        return createFieldID(mExportVarIdx_yellowSaturation, null);
    }

    private final static int mExportVarIdx_greenSaturation = 2;
    private float mExportVar_greenSaturation;
    public synchronized void set_greenSaturation(float v) {
        setVar(mExportVarIdx_greenSaturation, v);
        mExportVar_greenSaturation = v;
    }

    public float get_greenSaturation() {
        return mExportVar_greenSaturation;
    }

    public Script.FieldID getFieldID_greenSaturation() {
        return createFieldID(mExportVarIdx_greenSaturation, null);
    }

    private final static int mExportVarIdx_cyanSaturation = 3;
    private float mExportVar_cyanSaturation;
    public synchronized void set_cyanSaturation(float v) {
        setVar(mExportVarIdx_cyanSaturation, v);
        mExportVar_cyanSaturation = v;
    }

    public float get_cyanSaturation() {
        return mExportVar_cyanSaturation;
    }

    public Script.FieldID getFieldID_cyanSaturation() {
        return createFieldID(mExportVarIdx_cyanSaturation, null);
    }

    private final static int mExportVarIdx_blueSaturation = 4;
    private float mExportVar_blueSaturation;
    public synchronized void set_blueSaturation(float v) {
        setVar(mExportVarIdx_blueSaturation, v);
        mExportVar_blueSaturation = v;
    }

    public float get_blueSaturation() {
        return mExportVar_blueSaturation;
    }

    public Script.FieldID getFieldID_blueSaturation() {
        return createFieldID(mExportVarIdx_blueSaturation, null);
    }

    private final static int mExportVarIdx_magentaSaturation = 5;
    private float mExportVar_magentaSaturation;
    public synchronized void set_magentaSaturation(float v) {
        setVar(mExportVarIdx_magentaSaturation, v);
        mExportVar_magentaSaturation = v;
    }

    public float get_magentaSaturation() {
        return mExportVar_magentaSaturation;
    }

    public Script.FieldID getFieldID_magentaSaturation() {
        return createFieldID(mExportVarIdx_magentaSaturation, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_hue_saturation = 1;
    public Script.KernelID getKernelID_hue_saturation() {
        return createKernelID(mExportForEachIdx_hue_saturation, 1, null, null);
    }

    public void forEach_hue_saturation(Allocation ain) {
        forEach_hue_saturation(ain, null);
    }

    public void forEach_hue_saturation(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_hue_saturation, ain, null, null, sc);
    }

}

