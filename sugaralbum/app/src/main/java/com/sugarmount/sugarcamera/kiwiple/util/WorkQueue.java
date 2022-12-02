
package com.sugarmount.sugarcamera.kiwiple.util;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class WorkQueue {
    private static final String TAG = WorkQueue.class.getSimpleName();
    private static int N_THREAD = 4;
    private final PoolWorker[] threads;
    // queue는 instance별로 관리하기 위해서 static으로 하지 않는다.
    private final LinkedList<PoolWorkerRunnable> queue;

    private static WorkQueue sInstance;

    private WorkQueue() {
        queue = new LinkedList<PoolWorkerRunnable>();
        threads = new PoolWorker[N_THREAD];
        synchronized(threads) {
            for(int i = 0; i < N_THREAD; i++) {
                threads[i] = new PoolWorker("WorkQueue:#" + i);
                threads[i].start();
            }
        }
    }

    public static WorkQueue getInstance() {
        if(sInstance == null) {
            sInstance = new WorkQueue();
        }
        return sInstance;
    }

    /**
     * onPause에서 호출
     */
    @SuppressWarnings("unused")
    public void removeAll() {
        synchronized(queue) {
            queue.clear();
            try {
                Runnable r;
                while(true) {
                    r = queue.remove();
                    r = null;
                }
            } catch(NoSuchElementException e) {
            }
        }
    }

    public void execute(PoolWorkerRunnable r) {
        synchronized(queue) {
            if(queue.remove(r)) {
                SmartLog.getInstance().d(TAG, "removed from queue: " + r.toString());
                queue.addFirst(r);
            } else {
                queue.addLast(r);
            }
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {
        public PoolWorker(String threadName) {
            super(threadName);
        }

        @Override
        public void run() {
            Runnable r;
            while(true) {
                synchronized(queue) {
                    while(queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch(InterruptedException ignored) {
                        }
                    }
                    r = queue.removeFirst();
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    r.run();
                    r = null;
                } catch(RuntimeException e) {
                    // You might want to log something here
                    SmartLog.getInstance().e(TAG, "PoolWorker", e);
                }
            }
        }
    }
}
