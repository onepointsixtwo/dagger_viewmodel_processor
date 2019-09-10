package com.onepointsixtwo.dagger_viewmodel_processor;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;

public abstract class BaseClass {

    protected TypeName factoryType = ClassName.bestGuess("androidx.lifecycle.ViewModelProvider.Factory");
    protected TypeName javaObjectType = ClassName.bestGuess("java.lang.Object");
    protected TypeName hashmapClassName = ClassName.bestGuess("java.util.HashMap");
    protected TypeName classTypeName = ClassName.bestGuess("java.lang.Class");

    abstract TypeName classType();

    void outputClassToFiler(Filer filer) throws IOException {
        JavaFile.builder("com.dagger_view_model", getTypeBuilder().build()).build().writeTo(filer);
    }

    protected abstract TypeSpec.Builder getTypeBuilder();
}
