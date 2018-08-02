package ipc.processor;

import com.build.BindInterface;
import com.google.auto.common.MoreTypes;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import ipc.annotations.RxIpcInterface;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreElements.asType;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

@AutoService(Processor.class)
public class RxIpcProcessor extends LogIpcProcessor {

    private final Map<String, IGenerateBuilder> clientBuilders = new HashMap<>();

    private final Map<String, ExecuteBuilder> serverExecuteBuilders = new HashMap<>();

    private void makeFiles() {
        for (Map.Entry<String, IGenerateBuilder> entry : clientBuilders.entrySet()) {
            entry.getValue().generate(processingEnv);
        }

        for (Map.Entry<String, ExecuteBuilder> entry : serverExecuteBuilders.entrySet()) {
            entry.getValue().generate(processingEnv);
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        clientBuilders.clear();
        serverExecuteBuilders.clear();
        for (Element elem : roundEnvironment.getElementsAnnotatedWith(RxIpcInterface.class)) {
            if (elem.getKind() == ElementKind.INTERFACE) {
                BindInterface bindInterface = new BindInterface((TypeElement) elem);


                Set<DeclaredType> providerInterfaces = getValueFieldOfClasses(getAnnotationMirror(elem, RxIpcInterface.class));
                boolean isServer = false;
                if (providerInterfaces.size() == 1) {
                    for (DeclaredType providerInterface : providerInterfaces) {
                        bindInterface.setImplMirror(providerInterface.asElement().asType());
                        TypeElement providerType = MoreTypes.asTypeElement(providerInterface);
                        if (providerType.getInterfaces().contains(elem.asType())) {
                            isServer = true;
                            break;
                        }
                    }
                }

                RxIpcInterface annotation = elem.getAnnotation(RxIpcInterface.class);
                if (annotation.client()) {
                    final String key = elem.getClass().getName() + "client";
                    IGenerateBuilder builder = clientBuilders.get(key);
                    if (builder == null) {
                        builder = new ClientBuilder(bindInterface);
                        clientBuilders.put(key, builder);
                    }
                }
                if (isServer) {
                    final String key = elem.getClass().getName() + "server";
                    ExecuteBuilder builder = serverExecuteBuilders.get(key);
                    if (builder == null) {
                        builder = new ExecuteBuilder(bindInterface);
                        serverExecuteBuilders.put(key, builder);
                    }
                }
            }

        }

        makeFiles();
        System.out.println("1111 " + processingEnv.getOptions());
        System.out.println("1111 " + processingEnv.getFiler());
        return false;
    }

    private static AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            TypeElement annotationTypeElement = asType(annotationMirror.getAnnotationType().asElement());
            if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
                return annotationMirror;
            }
        }
        return null;
    }


    private ImmutableSet<DeclaredType> getValueFieldOfClasses(AnnotationMirror annotationMirror) {
        return getAnnotationValue(annotationMirror, "serverImpl")
                .accept(
                        new SimpleAnnotationValueVisitor8<ImmutableSet<DeclaredType>, Void>() {
                            @Override
                            public ImmutableSet<DeclaredType> visitType(TypeMirror typeMirror, Void v) {
                                return ImmutableSet.of(MoreTypes.asDeclared(typeMirror));
                            }

                            @Override
                            public ImmutableSet<DeclaredType> visitArray(
                                    List<? extends AnnotationValue> values, Void v) {
                                return values
                                        .stream()
                                        .flatMap(value -> value.accept(this, null).stream())
                                        .collect(toImmutableSet());
                            }
                        },
                        null);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<>();
        annotations.add(RxIpcInterface.class.getCanonicalName());
        return annotations;
    }
}
