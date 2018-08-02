package ipc.client;

import android.os.RemoteException;

import ipc.Request;

import static ipc.Ok.ERROR_REMOTE;


/**
 * created by sfx on 2018/3/9.
 */

class AsyncCall implements Runnable {
     final Request request;
     final Callback callback;
     final IpcClient client;

    AsyncCall(IpcClient client, Request request, Callback callback) {
        this.client = client;
        this.request = request;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            client.doRequest(request, callback);
        } catch (RemoteException e) {
            e.printStackTrace();
            callback.onFailure(ERROR_REMOTE, "RemoteException");
        } finally {
            client.dispatcher().finished(this);
        }
    }

}
