package ipc.processor;

import com.build.BindInterface;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class ExecuteBuilder implements IGenerateBuilder {

    private final BindInterface bindInterface;

    public ExecuteBuilder(BindInterface bindInterface) {
        this.bindInterface = bindInterface;
    }

    @Override
    public void generate(ProcessingEnvironment processingEnv) {

        String className = getClassName(bindInterface.getClassName());
        TypeSpec.Builder builder = TypeSpec.classBuilder(className);
        //
        final TypeName bindInterfaceTypeName = TypeName.get(bindInterface.getTypeMirror());
        final String paramName = bindInterface.getClassName().replaceFirst("I", "").toLowerCase();
        builder.addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(bindInterfaceTypeName, paramName)
                        .addStatement("this.$N = $N", paramName, paramName)
                        .build());
        //
        builder.addField(FieldSpec.builder(bindInterfaceTypeName, paramName, Modifier.PRIVATE, Modifier.FINAL).build());
        //
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        bindInterface.getMethodMap().values().forEach(method -> {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getName())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameters(method.getPrams())
                    .returns(TypeName.get(method.getReturnType()));

            StringBuilder statement = new StringBuilder("return ");
            statement.append(paramName);
            statement.append(".");
            statement.append(method.getName());
            statement.append("(");
            final AtomicInteger i = new AtomicInteger();
            method.getPrams().forEach(parameterSpec -> {
                if (i.get() >= 1) {
                    statement.append(" ,");
                }
                statement.append(parameterSpec.name);
                i.getAndIncrement();
            });
            statement.append(")");
            methodBuilder.addStatement(statement.toString());
            methodSpecs.add(methodBuilder.build());
        });

        builder.addSuperinterface(bindInterfaceTypeName)
                .addMethods(methodSpecs);

        //

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("executeRequest")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(MyTypeName.Request, "request")
                .returns(ParameterizedTypeName.get(MyTypeName.Observable, MyTypeName.Response))
                .addStatement("$T method = request.getMethod()", MyTypeName.String);

        bindInterface.getMethodMap().values().forEach(method -> {
            methodBuilder.beginControlFlow("if ($S.equals($N))", method.getCode(), "method");
            StringBuilder returns = new StringBuilder("return this.");
            returns.append(method.getName());
            returns.append("(");
            if (method.getPrams() != null) {
                final AtomicInteger i = new AtomicInteger();
                method.getPrams().forEach(parameterSpec -> {
                    String asName;
                    if (parameterSpec.type instanceof ClassName) {
                        asName = ((ClassName) parameterSpec.type).simpleName();
                    } else {
                        asName = ((ClassName) parameterSpec.type.box()).simpleName();
                    }
                    methodBuilder.addStatement("$T $N = request.getAs$N($S)",
                            parameterSpec.type, parameterSpec.name, asName, parameterSpec.name);
                    if (i.get() >= 1) {
                        returns.append(", ");
                    }
                    returns.append(parameterSpec.name);
                    i.getAndIncrement();
                });
            }
            //  this.getUsers().map(new Function<List<User>, Response>() {
//                @Override
//                public Response apply(List<User> t) throws Exception {return Response.success(t);}
//            })

            TypeName typeName = ParameterizedTypeName.get(method.getReturnType());
            String parameterized = "";
            if (typeName instanceof ParameterizedTypeName) {

                parameterized = typeName.toString().replaceFirst(((ParameterizedTypeName) typeName).rawType.toString(), "");
            }
            if (parameterized.startsWith("<")) {
                parameterized = parameterized.substring(1, parameterized.length());
            }
            if (parameterized.endsWith(">")) {
                parameterized = parameterized.substring(0, parameterized.length() - 1);
            }
            returns.append(").map(new $T<$N,$T>(){@Override public Response apply($N t) throws Exception {return Response.success(t);}})");
            methodBuilder.addStatement(returns.toString(), MyTypeName.RxFunction, parameterized, MyTypeName.Response, parameterized);
//            methodBuilder.addStatement(CodeBlock.builder()
//                    .beginControlFlow(returns.toString(), MyTypeName.RxFunction, parameterized, MyTypeName.Response)
////                    .addStatement("@Override")
//                    .addStatement("public $T apply($N t) throws Exception {return $T.success(t);}"
//                            , MyTypeName.Response, parameterized, MyTypeName.Response)
//                    .endControlFlow()
//                    .addStatement(")").build());

            methodBuilder.endControlFlow();
        });

        methodBuilder.addStatement("return $T.just($T.error($T.ERROR_NOT_FIND_METHOD))"
                , MyTypeName.Observable, MyTypeName.Response, MyTypeName.OK);
        builder.addSuperinterface(MyTypeName.IExecute)
                .addMethod(methodBuilder.build());
        builder.addType(factory(className).build());


        JavaFile javaFile = JavaFile.builder(bindInterface.getPackageName(), builder.build())
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getClassName(String src) {
        return "Execute" + src.replaceFirst("I", "");
    }

    private TypeSpec.Builder factory(String className) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Factory")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC);
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build());
        builder.addField(FieldSpec.builder(MyTypeName.IExecute, "execute", Modifier.PRIVATE, Modifier.STATIC).build());
        builder.addMethod(MethodSpec.methodBuilder("create")
                .returns(MyTypeName.IExecute)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .beginControlFlow("if (execute == null) ")
                .beginControlFlow("synchronized ($N.class)", className)
                .beginControlFlow("if (execute == null) ")
                .addStatement("execute = new $N(new $T())", className, bindInterface.getImplMirror())
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return execute")
                .build());
        return builder;
    }

}
