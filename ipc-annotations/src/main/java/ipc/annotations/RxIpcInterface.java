package ipc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * created by sfx on 2018/3/28.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface RxIpcInterface {
    boolean client() default true;

    Class serverImpl() default EmptyImpl.class;
}
