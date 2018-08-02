package ipc;

import android.content.ContentValues;
import android.os.Messenger;
import android.util.Log;

import java.util.UUID;

/**
 * created by sfx on 2018/3/5.
 */

public final class Request {

    private String id;
    private String className;
    private String method;
    private ContentValues params;
    private Messenger replyTo;

    public Request() {
        this.id = UUID.randomUUID().toString();
    }

    public Request(String id, String className, String method, ContentValues params, Messenger replyTo) {
        this.id = id;
        this.className = className;
        this.method = method;
        this.params = params;
        this.replyTo = replyTo;
    }

    public String getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ContentValues getParams() {
        return params;
    }

    private void checkContentValues() {
        if (params == null) {
            params = new ContentValues();
        }
    }

    public void put(String key, String value) {
        checkContentValues();
        params.put(key, value);
    }

    public void put(String key, Integer value) {
        checkContentValues();
        params.put(key, value);
    }

    public void put(String key, Boolean value) {
        checkContentValues();
        params.put(key, value);
    }

    public void put(String key, Long value) {
        checkContentValues();
        params.put(key, value);
    }

    public void put(String key, Float value) {
        checkContentValues();
        params.put(key, value);
    }

    public void put(String key, Double value) {
        checkContentValues();
        params.put(key, value);
    }

    public void put(String key, Short value) {
        checkContentValues();
        params.put(key, value);
    }

    public void put(String key, Byte value) {
        checkContentValues();
        params.put(key, value);
    }

    public Byte getAsByte(String key) {
        return params == null ? null : params.getAsByte(key);
    }

    public Short getAsShort(String key) {
        return params == null ? null : params.getAsShort(key);
    }

    public String getAsString(String key) {
        return params == null ? null : params.getAsString(key);
    }

    public Integer getAsInteger(String key) {
        return params == null ? null : params.getAsInteger(key);
    }

    public Boolean getAsBoolean(String key) {
        return params == null ? null : params.getAsBoolean(key);
    }

    public Long getAsLong(String key) {
        return params == null ? null : params.getAsLong(key);
    }

    public Float getAsFloat(String key) {
        return params == null ? null : params.getAsFloat(key);
    }

    public Double getAsDouble(String key) {
        return params == null ? null : params.getAsDouble(key);
    }

    public Messenger getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Messenger replyTo) {
        this.replyTo = replyTo;
    }

    public Class<?>[] getParamTypes() {
        if (params == null) return null;
        Class[] classes = new Class[params.size()];
        int i = 0;
        for (Object obj : params.valueSet()) {
            classes[i] = obj.getClass();
            i++;
        }
        return classes;
    }

}
