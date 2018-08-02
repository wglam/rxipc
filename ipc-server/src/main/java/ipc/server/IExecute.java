package ipc.server;

import io.reactivex.Observable;
import ipc.Request;

public interface IExecute {
    Observable<Response> executeRequest(Request request);

    interface Factory {
        IExecute create();
    }
}
