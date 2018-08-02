package ipc.rx;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import ipc.Request;
import ipc.client.IpcClient;

public class IpcObservableOnSubscribe<T> implements ObservableOnSubscribe<T> {
    private final IpcClient client;
    private final Request request;


    private IpcObservableOnSubscribe(IpcClient client, Request request) {
        this.client = client;
        this.request = request;


        Log.e("Api", "IpcObservableOnSubscribe " + this.getClass().getGenericSuperclass().toString());
    }


    @Override
    public void subscribe(ObservableEmitter<T> emitter) {
        client.enqueue(request, new RxCallBack<>(emitter));
    }

    public static <T> Observable<T> create(IpcClient client, Request request) {
        return Observable.create(new IpcObservableOnSubscribe<T>(client, request));
    }
}
