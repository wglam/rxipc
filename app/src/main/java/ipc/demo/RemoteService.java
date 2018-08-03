package ipc.demo;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.sunfeixiang.rxremote.BuildConfig;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class RemoteService extends IntentService {

    RxUserInfoClient client;

    public RemoteService() {
        super("RemoteService:remote");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new RxUserInfoClient(this, BuildConfig.APPLICATION_ID);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        exeMethod();
    }

    @Override
    public void onDestroy() {
        client.stop();
        super.onDestroy();
    }

    int position = 0;

    public void exeMethod() {
        Disposable disposable = client.getUsers()

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Exception {

                        Log.e("Api", String.valueOf(position) + "remote getUsers :" + users);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("Api", "remote " + String.valueOf(position) + " :" + throwable.getMessage());
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

                            Log.e("getUser", String.valueOf(position) + "remote getUser :" + user);
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
