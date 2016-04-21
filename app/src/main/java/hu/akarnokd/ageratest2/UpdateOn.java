package hu.akarnokd.ageratest2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.google.android.agera.Observable;
import com.google.android.agera.Updatable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Signals the update() coming from an upstream Observable on the specified Looper.
 */
public final class UpdateOn implements Observable {

    final Observable source;

    final Looper looper;

    final Map<Updatable, UpdateOnUpdatable> map;

    public UpdateOn(Observable source, Looper looper) {
        this.source = source;
        this.looper = looper;
        this.map = new HashMap<>();
    }


    @Override
    public void addUpdatable(@NonNull Updatable updatable) {
        UpdateOnUpdatable u;
        synchronized (this) {
            if (map.containsKey(updatable)) {
                throw new IllegalStateException("Updatable already added");
            }

            u = new UpdateOnUpdatable(updatable, looper);
            map.put(updatable, u);
        }
        source.addUpdatable(u);
    }

    @Override
    public void removeUpdatable(@NonNull Updatable updatable) {
        UpdateOnUpdatable u;
        synchronized (this) {
            u = map.remove(updatable);
            if (u == null) {
                throw new IllegalStateException("Updatable already removed");
            }
        }
        u.cancelled = true;
        source.removeUpdatable(u);
    }

    static final class UpdateOnUpdatable implements Updatable, Handler.Callback {
        final Updatable actual;

        final Handler handler;

        final AtomicLong count;

        volatile boolean cancelled;

        UpdateOnUpdatable(Updatable actual, Looper looper) {
            this.actual = actual;
            this.count = new AtomicLong();
            this.handler = new Handler(looper, this);
        }

        @Override
        public void update() {
            if (count.getAndIncrement() == 0) {
                Message msg = handler.obtainMessage();
                handler.dispatchMessage(msg);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            long c = count.get();

            for (;;) {
                for (long i = 0; i < c; i++) {
                    if (cancelled) {
                        break;
                    }
                    actual.update();
                }

                c = count.addAndGet(-c);
                if (c == 0L) {
                    break;
                }
            }

            return false;
        }
    }
}
