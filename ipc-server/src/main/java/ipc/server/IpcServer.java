package ipc.server;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ipc.Ok;
import ipc.Request;

import static ipc.Ok.KEY_CLASSNAME;
import static ipc.Ok.KEY_CODE;
import static ipc.Ok.KEY_ID;
import static ipc.Ok.KEY_MESSAGE;
import static ipc.Ok.KEY_METHOD;
import static ipc.Ok.KEY_PARAMS;


public abstract class IpcServer extends Service implements Handler.Callback {

    private Messenger messenger;
    private CompositeDisposable mDisposable;

    private void addDisposable(Disposable disposable) {
        if (mDisposable == null) {
            synchronized (this) {
                if (mDisposable == null) {
                    mDisposable = new CompositeDisposable();
                }
            }
        }
        mDisposable.add(disposable);
    }


    private void clearDisposable() {
        if (mDisposable != null) {
            mDisposable.clear();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        if (messenger == null) {
            messenger = new Messenger(new Handler(this));
        }
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        clearDisposable();
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            final Bundle bundle = message.getData();
            bundle.setClassLoader(ipc.Request.class.getClassLoader());
            final String id = bundle.getString(KEY_ID);
            final String clz = bundle.getString(KEY_CLASSNAME);
            final String method = bundle.getString(KEY_METHOD);
            final ContentValues params = bundle.getParcelable(KEY_PARAMS);
            doAction(new Request(id, clz, method, params, message.replyTo));
        }
        return true;
    }

    void doAction(final Request request) {
        final Disposable disposable = dispatchExecute(request)
                .subscribeOn(Schedulers.io())
//                .doOnDispose(new Action() {
//                    @Override
//                    public void run() {
//                        doActionResponse(request, Response.error(Ok.ERROR_EXECUTE_CANCEL, ""));
//                    }
//                })
                .subscribe(new Consumer<Response>() {
                    @Override
                    public void accept(Response response) {
                        doActionResponse(request, response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        doActionResponse(request, Response.error(Ok.ERROR_EXECUTE_METHOD, throwable.getMessage()));
                    }
                });
        addDisposable(disposable);
    }

    void doActionResponse(Request request, Response response) {
        try {
            final Message reply = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString(KEY_ID, request.getId());
            if (response != null) {
                bundle.putInt(KEY_CODE, response.getCode());
                if (response.getMessage() != null) {
                    bundle.putString(KEY_MESSAGE, response.getMessage());
                }
                response.putInto(bundle);
            }
            reply.setData(bundle);
            request.getReplyTo().send(reply);
        } catch (Exception e) {
            doActionResponse(request, Response.error(Ok.ERROR_EXECUTE_FORMAT, e.getMessage()));
        }
    }


    public abstract Observable<Response> dispatchExecute(Request request);
}
