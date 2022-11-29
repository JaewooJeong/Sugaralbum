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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/opacity2.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_opacity2 extends ScriptC {
    private static final String __rs_resource_name = "opacity2";
    // Constructor
    public  ScriptC_opacity2(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_opacity2(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __F32;
    private Element __U8_4;
    private FieldPacker __rs_fp_F32;
    private final static int mExportVarIdx_alpha = 0;
    private float mExportVar_alpha;
    public synchronized void set_alpha(float v) {
        setVar(mExportVarIdx_alpha, v);
        mExportVar_alpha = v;
    }

    public float get_alpha() {
        return mExportVar_alpha;
    }

    public Script.FieldID getFieldID_alpha() {
        return createFieldID(mExportVarIdx_alpha, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_opacity = 1;
    public Script.KernelID getKernelID_opacity() {
        return createKernelID(mExportForEachIdx_opacity, 1, null, null);
    }

    public void forEach_opacity(Allocation ain) {
        forEach_opacity(ain, null);
    }

    public void forEach_opacity(Allocation ain, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        forEach(mExportForEachIdx_opacity, ain, null, null, sc);
    }

}

