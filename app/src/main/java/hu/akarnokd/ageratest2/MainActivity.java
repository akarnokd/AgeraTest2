package hu.akarnokd.ageratest2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.agera.MutableRepository;
import com.google.android.agera.Observables;
import com.google.android.agera.Repositories;
import com.google.android.agera.UpdateDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        tw.append("\nHi Agera\r\n");

        MutableRepository<Integer> repo = Repositories.mutableRepository(0);

        List<Integer> list = new ArrayList<>();

        repo.addUpdatable(() -> list.add(repo.get()));

        Observable.range(1, 100000)
                .subscribeOn(Schedulers.computation())
                .doOnNext(v -> repo.accept(v))
                .ignoreElements()
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> {
                    TextView tw2 = (TextView)findViewById(R.id.textView);
                    tw2.append("Done: " + list.size() + "\n");
                    long t1 = System.currentTimeMillis() - t;
                    tw2.append("Time: " + t1 + " ms\n");
                })
                .subscribe()
        ;

    }

    void rx() {

        long t = System.currentTimeMillis();

        TextView tw = (TextView)findViewById(R.id.textView);

        tw.append("\nHi Rx\r\n");

        BehaviorSubject<Integer> ps = BehaviorSubject.create(0);

        List<Integer> list = new ArrayList<>();

        ps.subscribe(v -> list.add(v));

        Observable.range(1, 100000)
                .subscribeOn(Schedulers.computation())
                .doOnNext(v -> ps.onNext(v))
                .ignoreElements()
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> {
                    TextView tw2 = (TextView)findViewById(R.id.textView);
                    tw2.append("Done: " + list.size() + "\n");
                    long t1 = System.currentTimeMillis() - t;
                    tw2.append("Time: " + t1 + " ms\n");
                })
                .subscribe()
        ;

    }

}
