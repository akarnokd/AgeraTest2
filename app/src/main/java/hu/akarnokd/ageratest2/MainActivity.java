package hu.akarnokd.ageratest2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.agera.MutableRepository;
import com.google.android.agera.Observables;
import com.google.android.agera.Repositories;
import com.google.android.agera.UpdateDispatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import hu.akarnokd.agera.Agera;
import hu.akarnokd.agera.BehaviorMutableRepository;
import hu.akarnokd.rxjava2.Scheduler;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agera();
            }
        });
        fab.setVisibility(View.INVISIBLE);

        findViewById(R.id.button).setOnClickListener(v -> {
            agera();
        });

        findViewById(R.id.button2).setOnClickListener(v -> {
            rx();
        });

        findViewById(R.id.button3).setOnClickListener(v -> {
            ((TextView)findViewById(R.id.textView)).setText("");
        });

        findViewById(R.id.button4).setOnClickListener(v -> {
            rxagera();
        });

        findViewById(R.id.button5).setOnClickListener(v -> {
            rx2();
        });

        findViewById(R.id.button6).setOnClickListener(v -> {
            asynctask();
        });

        Integer[] array = new Integer[]{1, 10, 100, 1000, 10000, 100000};

        SpinnerAdapter sa = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item,
                array);

        Spinner sp = (Spinner)findViewById(R.id.spinner);
        sp.setAdapter(sa);

        sp.setSelection(array.length - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void agera() {

        long t = System.currentTimeMillis();

        TextView tw = (TextView)findViewById(R.id.textView);

        Spinner sp = (Spinner)findViewById(R.id.spinner);

        int n = (Integer)sp.getSelectedItem();

        tw.append("\nHi Agera\r\n");

        MutableRepository<Integer> repo = Repositories.mutableRepository(0);

        List<Integer> list = new ArrayList<>();

        repo.addUpdatable(() -> list.add(repo.get()));

        Observable.range(1, n)
                .subscribeOn(Schedulers.computation())
                .doOnNext(v -> repo.accept(v))
                .ignoreElements()
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> {
                    TextView tw2 = (TextView)findViewById(R.id.textView);
                    tw2.append("Done: " + list.size() + "\n");
                    tw2.append("~ unique: " + new HashSet<>(list).size() + "\n");
                    long t1 = System.currentTimeMillis() - t;
                    tw2.append("Time: " + t1 + " ms\n");
                })
                .subscribe()
        ;

    }

    void rx() {

        Spinner sp = (Spinner)findViewById(R.id.spinner);

        int n = (Integer)sp.getSelectedItem();

        long t = System.currentTimeMillis();

        TextView tw = (TextView)findViewById(R.id.textView);

        tw.append("\nHi Rx");

        BehaviorSubject<Integer> ps = BehaviorSubject.create();

        List<Integer> list = new ArrayList<>();

        Switch sw = (Switch)findViewById(R.id.switch1);

        if (sw.isChecked()) {
            ps.observeOn(AndroidSchedulers.mainThread(), false, n)
                    .subscribe(v -> list.add(v));
            tw.append(" (observeOn)\n");
        } else {
            ps.subscribe(v -> list.add(v));
            tw.append("\n");
        }

        Observable.range(1, n)
                .subscribeOn(Schedulers.computation())
                .doOnNext(v -> ps.onNext(v))
                .ignoreElements()
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> {
                    TextView tw2 = (TextView)findViewById(R.id.textView);
                    tw2.append("Done: " + list.size() + "\n");
                    tw2.append("~ unique: " + new HashSet<>(list).size() + "\n");
                    long t1 = System.currentTimeMillis() - t;
                    tw2.append("Time: " + t1 + " ms\n");
                })
                .subscribe()
        ;

    }

    void rxagera() {

        BehaviorMutableRepository<Integer> repo = new BehaviorMutableRepository<>();

        Spinner sp = (Spinner)findViewById(R.id.spinner);

        int n = (Integer)sp.getSelectedItem();

        long t = System.currentTimeMillis();

        TextView tw = (TextView)findViewById(R.id.textView);

        tw.append("\nHi RxAgera");

        List<Integer> list = new ArrayList<>();

        Switch sw = (Switch)findViewById(R.id.switch1);

        if (sw.isChecked()) {
            repo.observeOnMain()
                    .consume(() -> list.add(repo.get()));
            tw.append(" (observeOn)\n");
        } else {
            repo.consume(() -> list.add(repo.get()));
            tw.append("\n");
        }

        int[] v = { 0 };

        Observable.range(1, n)
                .subscribeOn(Schedulers.computation())
                .doOnNext(e -> repo.accept(v[0]++))
                .ignoreElements()
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> {
                    TextView tw2 = (TextView)findViewById(R.id.textView);
                    tw2.append("Done: " + list.size() + "\n");
                    tw2.append("~ unique: " + new HashSet<>(list).size() + "\n");
                    long t1 = System.currentTimeMillis() - t;
                    tw2.append("Time: " + t1 + " ms\n");
                })
                .subscribe()
        ;

    }

    void rx2() {

        Spinner sp = (Spinner)findViewById(R.id.spinner);

        int n = (Integer)sp.getSelectedItem();

        long t = System.currentTimeMillis();

        TextView tw = (TextView)findViewById(R.id.textView);

        tw.append("\nHi Rx2");

        hu.akarnokd.rxjava2.subjects.BehaviorSubject<Integer> ps = hu.akarnokd.rxjava2.subjects.BehaviorSubject.create();

        List<Integer> list = new ArrayList<>();

        Switch sw = (Switch)findViewById(R.id.switch1);

        Scheduler mainThread = RxJava2MainThread.INSTANCE;

        if (sw.isChecked()) {
            ps.observeOn(mainThread, false, n)
                    .subscribe(v -> list.add(v));
            tw.append(" (observeOn)\n");
        } else {
            ps.subscribe(v -> list.add(v));
            tw.append("\n");
        }

        hu.akarnokd.rxjava2.Observable.range(1, n)
                .subscribeOn(hu.akarnokd.rxjava2.schedulers.Schedulers.computation())
                .doOnNext(v -> ps.onNext(v))
                .ignoreElements()
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(mainThread)
                .doAfterTerminate(() -> {
                    TextView tw2 = (TextView)findViewById(R.id.textView);
                    tw2.append("Done: " + list.size() + "\n");
                    tw2.append("~ unique: " + new HashSet<>(list).size() + "\n");
                    long t1 = System.currentTimeMillis() - t;
                    tw2.append("Time: " + t1 + " ms\n");
                })
                .subscribe()
        ;

    }

    void asynctask() {

        Spinner sp = (Spinner) findViewById(R.id.spinner);

        int n = (Integer) sp.getSelectedItem();

        TextView tw = (TextView) findViewById(R.id.textView);

        tw.append("\nHi AsyncTask");

        tw.append("\n");

        new AsyncTaskOperation().execute(n);

    }

    private class AsyncTaskOperation extends AsyncTask<Integer, Integer, String> {

        List<Integer> list;

        long t;

        @Override
        protected String doInBackground(Integer... params) {
            int size = params[0];

            for (int i = 0; i < size; i++) {
                publishProgress(i);
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            TextView tw2 = (TextView) findViewById(R.id.textView);
            tw2.append("Done: " + list.size() + "\n");
            tw2.append("~ unique: " + new HashSet<>(list).size() + "\n");
            long t1 = System.currentTimeMillis() - t;
            tw2.append("Time: " + t1 + " ms\n");
        }

        @Override
        protected void onPreExecute() {
            list = new ArrayList<>();
            t = System.currentTimeMillis();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            list.add(values[0]);
        }
    }

}
