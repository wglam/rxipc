package ipc.server;

import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ipc.Ok.KEY_BODY;
import static ipc.Ok.KEY_IS_ARRAY;
import static ipc.Ok.SUCCESS;

/**
 * created by sfx on 2018/3/13.
 */

public final class Response<T extends Parcelable> {
    private int code;
    private String message;
    private T body;
    private ArrayList<T> bodys;

    private Response(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private Response(int code, String message, T body) {
        this.code = code;
        this.message = message;
        this.body = body;
    }

    private Response(int code, String message, Collection<T> bodys) {
        this.code = code;
        this.message = message;
        this.bodys = new ArrayList<>(bodys);
    }

    int getCode() {
        return code;
    }

    String getMessage() {
        return message;
    }

    void putInto(Bundle bundle) {
        if (body != null) {
            bundle.putParcelable(KEY_BODY, body);
        } else if (bodys != null) {
            bundle.putBoolean(KEY_IS_ARRAY, true);
            bundle.putParcelableArrayList(KEY_BODY, bodys);
        }
    }


    public static <T extends Parcelable> Response success(T body) {
        return new Response<>(SUCCESS, null, body);
    }

    public static <T extends Parcelable> Response success(Collection<T> body) {
        return new Response<>(SUCCESS, null, body);
    }

    public static Response error(int code) {
        return new Response(code, "");
    }

    public static Response error(int code, String message) {
        return new Response(code, message);
    }
}
