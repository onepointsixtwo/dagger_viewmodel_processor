package com.onepointsixtwo.dagger_viewmodel_processor;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

/**
 * Creator for the base injector class.
 * Not particularly type-safe, but it basically downcasts to Object whatever the input is so it can be upcast to
 * Activity or Fragment to do the injection with the Factory.
 *
 * TODO: work out how to verify if a class is a subclass of fragment or activity for type-safety.
 */
public class BaseInjectorClass extends BaseClass {

    private String name = "BaseViewModelInjector";
    private TypeSpec.Builder builder;

    public BaseInjectorClass() {
        createBasicClass();
        addAbstractInjectionMethod();
    }

    private void createBasicClass() {
        builder = TypeSpec.classBuilder("BaseViewModelInjector")
                .addModifiers(Modifier.ABSTRACT);
    }

    private void addAbstractInjectionMethod() {
        MethodSpec superclassMethod = MethodSpec.methodBuilder("inject")
                .addParameter(javaObjectType, "injectee")
                .addParameter(factoryType, "factory")
                .addModifiers(Modifier.ABSTRACT).build();
        builder.addMethod(superclassMethod);
    }

    @Override
    TypeName classType() {
        return ClassName.bestGuess(name);
    }

    @Override
    protected TypeSpec.Builder getTypeBuilder() {
        return builder;
    }
}
