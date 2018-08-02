package ipc.demo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;


public class UserInfoImpl implements IUserInfo {

    @Override
    public Observable<User> getUser(int id, String name, boolean isDel, long time, short sex, double money) {
        return Observable.just(new User("张三"));
    }

    @Override
    public Observable<User> getUser(int id) {
        return Observable.just(new User(id + "张三"));
    }

    @Override
    public Observable<List<User>> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("张三"));
        return Observable.just(users);
    }
}
