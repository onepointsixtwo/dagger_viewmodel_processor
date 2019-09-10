package com.onepointsixtwo.dagger_viewmodel_processor;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Creates a specific subclass of injector for injecting a specific activity or fragment
 */
public class InjectorSubclass extends BaseClass {

    private String name;
    private TypeSpec.Builder injectorBuilder;
    private TypeName enclosingClassType;
    private VariableElement viewModelFieldInEnclosingClass;
    private BaseInjectorClass baseInjectorClass;
    private InjectViewModelProcessor.Type type;
    private boolean useActivityScope;

    public InjectorSubclass(TypeElement enclosingClassType,
            BaseInjectorClass baseInjectorClass,
            VariableElement viewModelFieldInEnclosingClass,
            InjectViewModelProcessor.Type enclosingClassTypeEnum,
            boolean useActivityScope) {
        this.baseInjectorClass = baseInjectorClass;
        this.viewModelFieldInEnclosingClass = viewModelFieldInEnclosingClass;
        this.type = enclosingClassTypeEnum;
        this.useActivityScope = useActivityScope;

        this.enclosingClassType = ClassName.get(enclosingClassType);
        name = enclosingClassType.getSimpleName() + "ViewModelInjector";

        createBasicClass();
        addInjectMethod();
    }

    private void createBasicClass() {
        injectorBuilder = TypeSpec.classBuilder(name)
                .superclass(baseInjectorClass.classType())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    private void addInjectMethod() {
        TypeName viewModelType = ClassName.get(viewModelFieldInEnclosingClass.asType());
        TypeName viewModelProviders = ClassName.bestGuess("androidx.lifecycle.ViewModelProviders");

        String methodName = "inject";
        String injecteeVariableName = "injectee";
        String factoryVariableName = "factory";

        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(javaObjectType, injecteeVariableName)
                .addParameter(factoryType, factoryVariableName)
                .addAnnotation(Override.class);

        if (type == InjectViewModelProcessor.Type.FRAGMENT_TYPE && useActivityScope) {
            injectMethodBuilder.addStatement("(($T)$L).$L = $T.of((($T)$L).getActivity(), $L).get($T.class)",
                    enclosingClassType,
                    injecteeVariableName,
                    viewModelFieldInEnclosingClass.getSimpleName(),
                    viewModelProviders,
                    enclosingClassType,
                    injecteeVariableName,
                    factoryVariableName,
                    viewModelType);
        } else {
            injectMethodBuilder.addStatement("(($T)$L).$L = $T.of(($T)$L, $L).get($T.class)",
                    enclosingClassType,
                    injecteeVariableName,
                    viewModelFieldInEnclosingClass.getSimpleName(),
                    viewModelProviders,
                    enclosingClassType,
                    injecteeVariableName,
                    factoryVariableName,
                    viewModelType);
        }

        MethodSpec injectMethod = injectMethodBuilder.build();

        injectorBuilder.addMethod(injectMethod);
    }

    @Override
    protected TypeSpec.Builder getTypeBuilder() {
        return injectorBuilder;
    }

    @Override
    TypeName classType() {
        return ClassName.bestGuess(name);
    }
}
