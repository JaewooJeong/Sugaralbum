
package com.kiwiple.imageframework.filter;

import android.os.Parcel;
import android.os.Parcelable;

public class FilterCreator implements Parcelable.Creator<Filter> {
    @Override
    public Filter[] newArray(int size) {
        return new Filter[size];
    }

    @Override
    public Filter createFromParcel(Parcel src) {
        return new Filter(src);
    }
}
