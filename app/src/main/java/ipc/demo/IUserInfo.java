package ipc.demo;

import java.util.List;

import io.reactivex.Observable;
import ipc.annotations.RxIpcInterface;


/**
 * created by sfx on 2018/3/28.
 */
@RxIpcInterface(serverImpl = UserInfoImpl.class)
public interface IUserInfo {

    Observable<User> getUser(int id, String name, boolean isDel, long time, short sex, double money);

    Observable<User> getUser(int id);

    Observable<List<User>> getUsers();

}
