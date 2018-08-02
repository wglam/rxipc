package ipc.processor;

import com.squareup.javapoet.ClassName;


public class MyTypeName {


    public final static ClassName Context = ClassName.get("android.content", "Context");
    public final static ClassName String = ClassName.get("java.lang", "String");
    public final static ClassName Request = ClassName.get("ipc", "Request");
    public final static ClassName Response = ClassName.get("ipc.server", "Response");
    public final static ClassName IpcObservableOnSubscribe = ClassName.get("ipc.rx", "IpcObservableOnSubscribe");
    public final static ClassName BaseIpcClient = ClassName.get("ipc.client", "IpcClient");
    public final static ClassName BaseIpcServer = ClassName.get("ipc.server", "IpcServer");
    public final static ClassName IExecute = ClassName.get("ipc.server", "IExecute");
    public final static ClassName IExecute_Factory = ClassName.get("ipc.server.IExecute", "Factory");
    public final static ClassName Observable = ClassName.get("io.reactivex", "Observable");
    public final static ClassName OK = ClassName.get("ipc", "Ok");
    public final static ClassName RxFunction = ClassName.get("io.reactivex.functions", "Function");
    public final static ClassName ReflectType = ClassName.get("java.lang.reflect", "Type");
}
