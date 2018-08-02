package ipc.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;

/**
 * created by sfx on 2018/3/28.
 */

public abstract class LogIpcProcessor extends AbstractProcessor {

    public final void logInfo(String info) {
        log(Diagnostic.Kind.NOTE, info);
    }

    final void log(Diagnostic.Kind kind, String info) {
        if (this.processingEnv == null) return;
        final Messager messager = this.processingEnv.getMessager();
        if (messager != null) {
            messager.printMessage(kind, getClass().getName() + "Building : " + info);
        }
    }

    public final void logError(String info) {
        log(Diagnostic.Kind.ERROR, info);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
