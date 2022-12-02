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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/posterize.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_posterize extends ScriptC {
    private static final String __rs_resource_name = "posterize";
    // Constructor
    public  ScriptC_posterize(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_posterize(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __F32;
    private Element __U8_4;
    private FieldPacker __rs_fp_F32;
    private final static int mExportVarIdx_colorLevels = 0;
    private float mExportVar_colorLevels;
    public synchronized void set_colorLevels(float v) {
        setVar(mExportVarIdx_colorLevels, v);
        mExportVar_colorLevels = v;
    }

    public float get_colorLevels() {
        return mExportVar_colorLevels;
    }

    public Script.FieldID getFieldID_colorLevels() {
        return createFieldID(mExportVarIdx_colorLevels, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_posterize = 1;
    public Script.KernelID getKernelID_posterize() {
        return createKernelID(mExportForEachIdx_posterize, 1, null, null);
    }

    public void forEach_posterize(Allocation ain) {
        forEach_posterize(ain, null);
    }

    public void forEach_posterize(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_posterize, ain, null, null, sc);
    }

}

