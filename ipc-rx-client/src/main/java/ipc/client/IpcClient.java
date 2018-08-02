package ipc.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ipc.Request;


import static android.content.Context.BIND_AUTO_CREATE;
import static ipc.Ok.ERROR;
import static ipc.Ok.KEY_BODY;
import static ipc.Ok.KEY_CLASSNAME;
import static ipc.Ok.KEY_CODE;
import static ipc.Ok.KEY_ID;
import static ipc.Ok.KEY_MESSAGE;
import static ipc.Ok.KEY_METHOD;
import static ipc.Ok.KEY_PARAMS;
import static ipc.Ok.SUCCESS;

/**
 * created by sfx on 2018/3/5.
 */
public class IpcClient implements ServiceConnection, Handler.Callback, DeathRecipient {

    private final Context context;
    private final Messenger call;
    private volatile Messenger server;
    private Map<String, Callback> callbackMap = new ConcurrentHashMap<>();
    private Dispatcher dispatcher;
    private volatile boolean connected = false;
    private volatile boolean binding = false;

    synchronized Dispatcher dispatcher() {
        if (dispatcher == null) {
            synchronized (Dispatcher.class) {
                if (dispatcher == null) {
                    dispatcher = new Dispatcher();
                }
            }
        }
        return dispatcher;
    }

    private String serviceAction;
    private String applicationId;

    public IpcClient(Context context, @NonNull String applicationId) {
        this.context = context.getApplicationContext();
        this.serviceAction = "ipc.server";
        this.applicationId = applicationId;
        call = new Messenger(new Handler(IpcClient.this));
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        server = new Messenger(iBinder);
        dispatcher().promoteCalls();
        connected = true;
        try {
            iBinder.linkToDeath(this, 0);
        } catch (RemoteException e) {
            binderDied();
        }
        Log.e("Api", "onServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        binderDied();
        Log.e("Api", "onServiceDisconnected");

    }

    @Override
    public void onBindingDied(ComponentName name) {
        binderDied();
        Log.e("Api", "onBindingDied");
    }

    @Override
    public final void binderDied() {
        server = null;
        connected = false;
        binding = false;
        Log.e("Api", "binderDied()");
    }

    public void stop() {
        clearDisposable();
        callbackMap.clear();
        dispatcher().cancelAll();
        disconnect();
    }

    private void connect() {
        synchronized (this) {
            if (context == null || binding || connected) return;
            Intent intent = new Intent(serviceAction);
            intent.setPackage(applicationId);
            context.bindService(intent, this, BIND_AUTO_CREATE);
            binding = true;
            Log.e("Api", "bindService");
        }
    }

    private void disconnect() {
        synchronized (this) {
            if (context != null && binding) {
                context.unbindService(this);
                binding = false;
                Log.e("Api", "unBindService" + connected);
            }
        }
    }

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
    public boolean handleMessage(Message msg) {
        final Bundle bundle = msg.getData();
        bundle.setClassLoader(getClass().getClassLoader());
        final String id = bundle.getString(KEY_ID);
        final int code = bundle.getInt(KEY_CODE, ERROR);
        final String message = bundle.getString(KEY_MESSAGE);

        handResponse(id, code, message, bundle);
        return true;
    }


    private void handResponse(String requestId, int code, String message, Bundle bundle) {
        if (requestId != null) {
            final Callback callback = callbackMap.remove(requestId);
            if (callback == null) return;

            if (code == SUCCESS) {
                callback.onResponse(bundle);
            } else {
                callback.onFailure(code, message);
            }
        }
        if (callbackMap.isEmpty()) {
            disconnect();
        }
    }

    public final void enqueue(Request request, Callback callback) {
        connect();
        dispatcher().enqueue(new AsyncCall(this, request, callback), connected);
    }

    void doRequest(Request request, Callback callback) throws RemoteException {
        callbackMap.put(request.getId(), callback);
        Message msg = Message.obtain();
        final Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, request.getId());
        bundle.putString(KEY_CLASSNAME, request.getClassName());
        bundle.putString(KEY_METHOD, request.getMethod());
        bundle.putParcelable(KEY_PARAMS, request.getParams());
        msg.setData(bundle);
        msg.replyTo = call;
        server.send(msg);
    }
}
