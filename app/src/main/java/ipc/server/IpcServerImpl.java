package ipc.server;

import io.reactivex.Observable;
import ipc.Request;
import ipc.demo.ExecuteUserInfo;
import ipc.demo.IUserInfo;

import static ipc.Ok.ERROR_NOT_FIND_SERVER;

public class IpcServerImpl extends IpcServer {
    @Override
    public Observable<Response> dispatchExecute(Request request) {
        try {
            if (IUserInfo.class.getName().equals(request.getClassName())) {
                return new ExecuteUserInfo.Factory().create().executeRequest(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Observable.just(Response.error(ERROR_NOT_FIND_SERVER, ""));
    }
}
