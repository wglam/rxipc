package ipc.rx;


import android.os.Bundle;


import io.reactivex.ObservableEmitter;
import ipc.Ok;
import ipc.client.Callback;


/**
 * created by sfx on 2018/3/19.
 */

class RxCallBack<T> implements Callback {
    private final ObservableEmitter<? super T> emitter;


    RxCallBack(ObservableEmitter<? super T> emitter) {
        this.emitter = emitter;
    }


    @Override
    public void onResponse(Bundle data) {
        try {
            if (data.getBoolean(Ok.KEY_IS_ARRAY)) {
                emitter.onNext((T) data.getParcelableArrayList(Ok.KEY_BODY));
            } else {
                emitter.onNext((T) data.getParcelable(Ok.KEY_BODY));
            }
        } catch (Exception e) {
            emitter.onError(new Throwable(e.getMessage()));
        }
    }

    @Override
    public void onFailure(int code, String message) {
        emitter.onError(new Throwable(code + "" + message));
    }
}
