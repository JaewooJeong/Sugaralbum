
package com.sugarmount.sugarcamera.kiwiple.util;

public class PoolWorkerRunnable implements Runnable {
    private int mKey;

    public PoolWorkerRunnable(int key) {
        mKey = key;
    }

    @Override
    public void run() {
    }

    @Override
    public int hashCode() {
        return mKey;
    }

    @Override
    public boolean equals(Object o) {
        if(mKey == -1) {
            return false;
        }
        if(o instanceof PoolWorkerRunnable && ((PoolWorkerRunnable)o).mKey == mKey) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Key:" + mKey;
    }
}
