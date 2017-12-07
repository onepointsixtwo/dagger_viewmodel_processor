package com.onepointsixtwo.dagger_viewmodel_processor;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

public class ViewModelInjectorsClass extends BaseClass {

    private static final String name = "ViewModelInjectors";
    private TypeSpec.Builder typeBuilder;
    private BaseInjectorClass baseInjectorClass;
    private String mapVariableName = "map";
    private TypeName supportActivityType = ClassName.bestGuess("android.support.v4.app.ActivityCompat");
    private TypeName supportFragmentType = ClassName.bestGuess("android.support.v4.app.Fragment");
    private TypeName activityType = ClassName.bestGuess("android.app.Activity");
    private TypeName fragmentType = ClassName.bestGuess("android.app.Fragment");

    public ViewModelInjectorsClass(HashMap<TypeName, TypeName> classToInjectorTypeMapping, BaseInjectorClass baseInjectorClass) {
        this.baseInjectorClass = baseInjectorClass;
        createBasicClass();
        createGetHashmapOfClassToInjectorMethod(classToInjectorTypeMapping);
        createInjectClassActivityMethod();
        createInjectClassFragmentMethod();
        createInjectClassSupportActivityMethod();
        createInjectClassSupportFragmentMethod();
    }

    private void createBasicClass() {
        typeBuilder = TypeSpec.classBuilder("ViewModelInjectors")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    /**
     * Creates a method on this class to build a hashmap of the class an injector can inject, to the injector itself mapping
     */
    private void createGetHashmapOfClassToInjectorMethod(HashMap<TypeName, TypeName> classToInjectorTypeMapping) {
        MethodSpec.Builder getInjectorsMethod = MethodSpec.methodBuilder("getInjectorsForClass")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC);

        //Create the hashmap
        String mapVariableName = "map";
        getInjectorsMethod.addStatement("$T<$T, $T> $L = new $T<>()", hashmapClassName, classTypeName, baseInjectorClass.classType(), mapVariableName, hashmapClassName);

        //Add the values to it - the class maps to the instance of the injector for it
        for(Map.Entry<TypeName, TypeName> entry: classToInjectorTypeMapping.entrySet()) {
            TypeName fragmentOrActivityType = entry.getKey();
            TypeName injectorType = entry.getValue();
            getInjectorsMethod
                    .addStatement("$L.put($T.class, new $T())", mapVariableName, fragmentOrActivityType, injectorType);
        }

        //Handle return statement to return created hashmap
        getInjectorsMethod.addStatement("return $L", mapVariableName);
        getInjectorsMethod.returns(hashmapClassName);

        //Add the method
        typeBuilder.addMethod(getInjectorsMethod.build());
    }

    private void createInjectClassFragmentMethod() {
        String injecteeParamName = "fragment";
        String factoryParamName = "factory";
        String injectorVariableName = "injector";

        MethodSpec.Builder inject = MethodSpec.methodBuilder("inject");
        inject.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(fragmentType, injecteeParamName)
                .addParameter(factoryType, factoryParamName);

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement("$T<$T, $T> $L = getInjectorsForClass()", hashmapClassName, classTypeName, baseInjectorClass.classType(), mapVariableName);
        inject.addStatement("$T $L = $L.get($L.getClass())", baseInjectorClass.classType(), injectorVariableName, mapVariableName, injecteeParamName);
        inject.addStatement("if ($L != null) { $L.inject($L, $L); }", injectorVariableName, injectorVariableName, injecteeParamName, "factory");

        typeBuilder.addMethod(inject.build());
    }

    private void createInjectClassActivityMethod() {
        String injecteeParamName = "activity";
        String factoryParamName = "factory";
        String injectorVariableName = "injector";

        MethodSpec.Builder inject = MethodSpec.methodBuilder("inject");
        inject.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(activityType, injecteeParamName)
                .addParameter(factoryType, factoryParamName);

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement("$T<$T, $T> $L = getInjectorsForClass()", hashmapClassName, classTypeName, baseInjectorClass.classType(), mapVariableName);
        inject.addStatement("$T $L = $L.get($L.getClass())", baseInjectorClass.classType(), injectorVariableName, mapVariableName, injecteeParamName);
        inject.addStatement("if ($L != null) { $L.inject($L, $L); }", injectorVariableName, injectorVariableName, injecteeParamName, "factory");

        typeBuilder.addMethod(inject.build());
    }

    private void createInjectClassSupportFragmentMethod() {
        String injecteeParamName = "supportFragment";
        String factoryParamName = "factory";
        String injectorVariableName = "injector";

        MethodSpec.Builder inject = MethodSpec.methodBuilder("inject");
        inject.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(supportFragmentType, injecteeParamName)
                .addParameter(factoryType, factoryParamName);

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement("$T<$T, $T> $L = getInjectorsForClass()", hashmapClassName, classTypeName, baseInjectorClass.classType(), mapVariableName);
        inject.addStatement("$T $L = $L.get($L.getClass())", baseInjectorClass.classType(), injectorVariableName, mapVariableName, injecteeParamName);
        inject.addStatement("if ($L != null) { $L.inject($L, $L); }", injectorVariableName, injectorVariableName, injecteeParamName, "factory");

        typeBuilder.addMethod(inject.build());
    }

    private void createInjectClassSupportActivityMethod() {
        String injecteeParamName = "supportActivity";
        String factoryParamName = "factory";
        String injectorVariableName = "injector";

        MethodSpec.Builder inject = MethodSpec.methodBuilder("inject");
        inject.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(supportActivityType, injecteeParamName)
                .addParameter(factoryType, factoryParamName);

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement("$T<$T, $T> $L = getInjectorsForClass()", hashmapClassName, classTypeName, baseInjectorClass.classType(), mapVariableName);
        inject.addStatement("$T $L = $L.get($L.getClass())", baseInjectorClass.classType(), injectorVariableName, mapVariableName, injecteeParamName);
        inject.addStatement("if ($L != null) { $L.inject($L, $L); }", injectorVariableName, injectorVariableName, injecteeParamName, "factory");

        typeBuilder.addMethod(inject.build());
    }


    @Override
    TypeName classType() {
        return ClassName.bestGuess(name);
    }

    @Override
    protected TypeSpec.Builder getTypeBuilder() {
        return typeBuilder;
    }
}
