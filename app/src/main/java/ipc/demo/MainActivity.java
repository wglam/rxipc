package ipc.demo;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import com.example.sunfeixiang.rxremote.BuildConfig;
import com.example.sunfeixiang.rxremote.R;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {


    RxUserInfoClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new RxUserInfoClient(this, BuildConfig.APPLICATION_ID);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        client.stop();
        super.onDestroy();
    }

    int position = 0;

    public void exeMethod(View view) {
        Intent intent = new Intent(this, RemoteService.class);
        startService(intent);
        Disposable disposable = client.getUsers()

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Exception {

                        Log.e("Api", String.valueOf(position) + " getUsers :" + users);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("Api", String.valueOf(position) + " :" + throwable.getMessage());
                    }
                });

        for (int i = 0; i < 1000; i++) {


//        }
            position++;
            Disposable disposable2 = client.getUser(position)

                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<User>() {
                        @Override
                        public void accept(User user) throws Exception {

                            Log.e("getUser", String.valueOf(position) + " getUser :" + user);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.e("getUser", String.valueOf(position) + " :" + throwable.getMessage());
                        }
                    });

        }
    }


}
