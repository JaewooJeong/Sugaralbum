
package com.kiwiple.imageframework.util.thread;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.kiwiple.imageframework.util.SmartLog;

/**
 * 4개의 쓰레드를 생성하여 비동기 처리를 관리하는 클래스<br>
 * {@link #getInstance} 함수로 인스턴스를 생성하고, 싱글톤으로 동작한다
 * 
 * @version 2.0
 */
public class WorkQueue {
    private static final String TAG = WorkQueue.class.getSimpleName();
    private static int N_THREAD = 4;
    private final PoolWorker[] threads;
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

    /**
     * @return {@link #WorkQueue}의 인스턴스 반환
     * @version 2.0
     */
    public static WorkQueue getInstance() {
        if(sInstance == null) {
            sInstance = new WorkQueue();
        }
        return sInstance;
    }

    /**
     * 진행 예정인 비동기 처리 작업을 모두 삭제한다.
     * 
     * @version 2.0
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

    /**
     * 비동기 처리 작업을 추가한다.
     * 
     * @param r
     * @version 2.0
     */
    public void execute(PoolWorkerRunnable r) {
        synchronized(queue) {
            if(queue.remove(r)) {
                SmartLog.d(TAG, "removed from queue: " + r.toString());
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
                    SmartLog.e(TAG, "PoolWorker", e);
                }
            }
        }
    }
}
