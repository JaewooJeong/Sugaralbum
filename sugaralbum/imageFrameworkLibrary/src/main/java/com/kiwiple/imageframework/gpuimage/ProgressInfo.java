
package com.kiwiple.imageframework.gpuimage;

public class ProgressInfo {
    public float maxValue;
    public float minValue;
    public float defaultValue;
    public float valueToProgress;
    public int progressMax;
    public int progressDefaut;
    public String progressName;
    public boolean needWeight = false;

    public ProgressInfo(float maxValue, float minValue, float defaultValue, float valueToProgress,
            String progressName) {
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.defaultValue = defaultValue;
        this.valueToProgress = valueToProgress;
        this.progressName = progressName;
        progressMax = (int)((maxValue - minValue) * valueToProgress);
        progressDefaut = (int)((defaultValue - minValue) * valueToProgress);
    }

    public ProgressInfo(float maxValue, float minValue, float defaultValue, float valueToProgress,
            String progressName, boolean needWeight) {
        this(maxValue, minValue, defaultValue, valueToProgress, progressName);
        this.needWeight = needWeight;
    }
}
