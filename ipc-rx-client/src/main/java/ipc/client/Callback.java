package ipc.client;

import android.os.Bundle;

/**
 * created by sfx on 2018/3/9.
 */

public interface Callback {
    void onResponse(Bundle data);

    void onFailure(int code, String message);
}
