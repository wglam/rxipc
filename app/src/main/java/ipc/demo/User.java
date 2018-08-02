package ipc.demo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * created by sfx on 2018/3/15.
 */

public class User implements Parcelable {
    private String name;

    public User(String name) {
        this.name = name;
    }


    protected User(Parcel in) {
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{name=$name}".replace("$name", name);
    }
}
