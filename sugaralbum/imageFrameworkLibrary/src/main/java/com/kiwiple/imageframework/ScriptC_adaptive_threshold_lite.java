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
 * The source Renderscript file: /Users/leeminchul/Documents/workspace_uplus_git/project/multimediaframework/ImageAnalysisEngine/ImageFrameworkLibrary/renderscript/adaptive_threshold_lite.rs
 */
package com.kiwiple.imageframework;

import androidx.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_adaptive_threshold_lite extends ScriptC {
    private static final String __rs_resource_name = "adaptive_threshold_lite";
    // Constructor
    public  ScriptC_adaptive_threshold_lite(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_adaptive_threshold_lite(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __ALLOCATION = Element.ALLOCATION(rs);
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private final static int mExportVarIdx_grayAllocation = 0;
    private Allocation mExportVar_grayAllocation;
    public synchronized void set_grayAllocation(Allocation v) {
        setVar(mExportVarIdx_grayAllocation, v);
        mExportVar_grayAllocation = v;
    }

    public Allocation get_grayAllocation() {
        return mExportVar_grayAllocation;
    }

    public Script.FieldID getFieldID_grayAllocation() {
        return createFieldID(mExportVarIdx_grayAllocation, null);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_adaptive_threshold = 1;
    public Script.KernelID getKernelID_adaptive_threshold() {
        return createKernelID(mExportForEachIdx_adaptive_threshold, 3, null, null);
    }

    public void forEach_adaptive_threshold(Allocation ain, Allocation aout) {
        forEach_adaptive_threshold(ain, aout, null);
    }

    public void forEach_adaptive_threshold(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
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
        forEach(mExportForEachIdx_adaptive_threshold, ain, aout, null, sc);
    }

}

