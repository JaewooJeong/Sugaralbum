
package com.kiwiple.imageframework.util.thread;

/**
 * {@link com.kiwiple.imageframework.util.thread#WorkQueue}의 쓰레드 풀에서 동작하는 {@link java.lang#Runnable}
 * 상속 클래스
 * 
 * @version 2.0
 */
public class PoolWorkerRunnable implements Runnable {
    private Object mKey;

    /**
     * @param key 고유 번호
     * @version 2.0
     */
    public PoolWorkerRunnable(Object key) {
        mKey = key;
    }

    @Override
    public void run() {
    }

    @Override
    public int hashCode() {
        return mKey.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(mKey == null) {
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
