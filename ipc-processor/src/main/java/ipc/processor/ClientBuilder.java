package ipc.processor;

import com.build.BindInterface;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class ClientBuilder implements IGenerateBuilder {
    private final BindInterface bindInterface;

    public ClientBuilder(BindInterface bindInterface) {
        this.bindInterface = bindInterface;
    }

    @Override
    public final void generate(ProcessingEnvironment processingEnv) {

        TypeSpec.Builder builder = TypeSpec.classBuilder(getClassName(bindInterface.getClassName()));
        //
        builder.addModifiers(Modifier.PUBLIC)
                .superclass(MyTypeName.BaseIpcClient)
                .addSuperinterface(TypeName.get(bindInterface.getTypeMirror()))
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(MyTypeName.Context, "context")
                        .addParameter(MyTypeName.String, "applicationId")
                        .addStatement("super(context, applicationId)")
                        .build());

        //
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        bindInterface.getMethodMap().values().forEach(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameters(method.getPrams())
                    .returns(TypeName.get(method.getReturnType()));
            methodBuilder.addStatement("$T request = new $T()", MyTypeName.Request, MyTypeName.Request)
                    .addStatement("request.setClassName($S)", bindInterface.getFullName())
                    .addStatement("request.setMethod($S)", method.getCode());
            if (method.getPrams() != null) {
                method.getPrams().forEach(parameterSpec ->
                        methodBuilder.addStatement("request.put($S, $N)", parameterSpec.name, parameterSpec.name));
            }

//            TypeName typeName = ParameterizedTypeName.get(method.getReturnType());
//            String t = typeName.toString();
//            if (typeName instanceof ParameterizedTypeName) {
//                methodBuilder.addStatement("return $T.$Ncreate(this, request)"
//                        , MyTypeName.IpcObservableOnSubscribe, typeName.toString().replaceFirst(((ParameterizedTypeName) typeName).rawType.toString(), ""));
//            } else {
//
//            }

            methodBuilder.addStatement("return $T.create(this, request)"
                    , MyTypeName.IpcObservableOnSubscribe);
            methodSpecs.add(methodBuilder.build());
        });

        builder.addMethods(methodSpecs);

        JavaFile javaFile = JavaFile.builder(bindInterface.getPackageName(), builder.build())
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getClassName(String src) {
        return "Rx" + src.replaceFirst("I", "") + "Client";
    }
}
