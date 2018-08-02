package com.build;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class BindInterface {

    private String fullName;
    private String className;
    private String packageName;
    private TypeMirror typeMirror;
    private TypeMirror implMirror;

    public TypeMirror getImplMirror() {
        return implMirror;
    }

    public void setImplMirror(TypeMirror implMirror) {
        this.implMirror = implMirror;
    }

    private final Map<String, Method> methodMap = new HashMap<>();

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFullName() {
        return fullName;
    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

    public BindInterface(TypeElement typeElement) {
        fullName = typeElement.getQualifiedName().toString();
        className = typeElement.getSimpleName().toString();
        packageName = fullName.substring(0, fullName.lastIndexOf("."));
        typeMirror = typeElement.asType();

        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                final String name = enclosedElement.getSimpleName().toString();
                Iterable<ParameterSpec> parameterSpecs = generateParameters(((ExecutableElement) enclosedElement).getParameters());
                final StringBuilder methodCode = new StringBuilder();
                methodCode.append(name);
                methodCode.append("(");
                parameterSpecs.forEach(parameterSpec -> {
                    methodCode.append(parameterSpec);
                    methodCode.append(",");
                });
                methodCode.append(")");
                final String code = methodCode.toString();
                Method method = new Method(code, name, ((ExecutableElement) enclosedElement).getReturnType(), parameterSpecs);
                methodMap.put(code, method);
            }
        }
    }

    private Iterable<ParameterSpec> generateParameters(List<? extends VariableElement> parameterElements) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        for (VariableElement parameter : parameterElements) {
            ParameterSpec spec = ParameterSpec.get(parameter);
            parameterSpecs.add(spec);
        }
        return parameterSpecs;
    }


    public TypeMirror getTypeMirror() {
        return typeMirror;
    }


    public static final class Method {
        private String code;
        private String name;
        private TypeMirror returnType;
        private final Iterable<ParameterSpec> prams;

        public Method(String code, String name, TypeMirror returnType, Iterable<ParameterSpec> prams) {
            this.code = String.valueOf(code.hashCode());
            this.name = name;
            this.returnType = returnType;
            this.prams = prams;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public TypeMirror getReturnType() {
            return returnType;
        }

        public Iterable<ParameterSpec> getPrams() {
            return prams;
        }
    }
}
