package hu.akarnokd.ageratest2;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import hu.akarnokd.rxjava2.Scheduler;
import hu.akarnokd.rxjava2.disposables.Disposable;
import hu.akarnokd.rxjava2.internal.disposables.EmptyDisposable;

/**
 * Main thread scheduler for RxJava 2
 */
public final class RxJava2MainThread extends Scheduler {

    public static final RxJava2MainThread INSTANCE = new RxJava2MainThread();

    @Override
    public Worker createWorker() {
        return new MainThreadWorker();
    }

    static final class MainThreadWorker extends Worker {

        final Handler handler = new Handler(Looper.getMainLooper());

        volatile boolean disposed;

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (disposed) {
                return EmptyDisposable.INSTANCE;
            }
            if (delay <= 0L) {
                handler.post(run);
            } else {
                handler.postDelayed(run, TimeUnit.MILLISECONDS.convert(delay, unit));
            }
            if (disposed) {
                handler.removeCallbacks(run);
                return EmptyDisposable.INSTANCE;
            }
            return () -> handler.removeCallbacks(run);
        }

        @Override
        public void dispose() {
            disposed = true;
            handler.removeCallbacksAndMessages(null);
        }
    }
}
