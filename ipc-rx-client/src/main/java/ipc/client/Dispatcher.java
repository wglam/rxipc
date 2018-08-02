package ipc.client;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

final class Dispatcher {
    private int maxRequests = 64;
    private ExecutorService executorService;
    private final Deque<AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    private final Deque<AsyncCall> runningAsyncCalls = new ArrayDeque<>();

    private synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }
        return executorService;
    }

    synchronized void enqueue(AsyncCall call, boolean execute) {
        if (execute && runningAsyncCalls.size() < maxRequests) {
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            readyAsyncCalls.add(call);
        }
    }


    synchronized void cancelAll() {
        readyAsyncCalls.clear();
        runningAsyncCalls.clear();
        if (executorService != null)
            executorService.shutdownNow();
    }

    synchronized final void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) return;
        if (readyAsyncCalls.isEmpty()) return;

        for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            AsyncCall call = i.next();
            i.remove();
            runningAsyncCalls.add(call);
            executorService().execute(call);
            if (runningAsyncCalls.size() >= maxRequests) return;
        }
    }

    void finished(AsyncCall call) {
        finished(runningAsyncCalls, call, true);
    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        synchronized (this) {
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            if (promoteCalls) promoteCalls();
        }
    }
}
