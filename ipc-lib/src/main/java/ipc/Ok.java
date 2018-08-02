package ipc;

/**
 * created by sfx on 2018/3/13.
 */

public final class Ok {
    public transient final static String KEY_ID = "id";
    public transient final static String KEY_CLASSNAME = "class";
    public transient final static String KEY_METHOD = "method";
    public transient final static String KEY_PARAMS = "params";
    public transient final static String KEY_BODY = "body";
    public transient final static String KEY_CODE = "code";
    public transient final static String KEY_MESSAGE = "message";
    public transient final static String KEY_IS_ARRAY = "array";
    public transient final static int SUCCESS = 200;

    public transient final static int ERROR = -1;
    public transient final static int ERROR_REMOTE = -2;
    public transient final static int ERROR_NOT_FIND_SERVER = -3;
    public transient final static int ERROR_NOT_FIND_METHOD = -4;
    public transient final static int ERROR_EXECUTE_METHOD = -5;
    public transient final static int ERROR_EXECUTE_CANCEL = -6;
    public transient final static int ERROR_EXECUTE_FORMAT = -7;

}
