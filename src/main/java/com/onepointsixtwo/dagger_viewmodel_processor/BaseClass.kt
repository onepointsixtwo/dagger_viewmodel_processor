package com.onepointsixtwo.dagger_viewmodel_processor


import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

import java.io.IOException

import javax.annotation.processing.Filer

abstract class BaseClass {

    protected var factoryType: TypeName =
        ClassName.bestGuess("androidx.lifecycle.ViewModelProvider.Factory")
    protected var javaObjectType: TypeName = ClassName.bestGuess("java.lang.Object")
    protected var hashmapClassName: TypeName = ClassName.bestGuess("java.util.HashMap")
    protected var classTypeName: TypeName = ClassName.bestGuess("kotlin.reflect.KClass")

    protected abstract val typeBuilder: TypeSpec.Builder?

    internal abstract fun classType(): TypeName

    @Throws(IOException::class)
    internal fun outputClassToFiler(filer: Filer) {
        FileSpec.builder("com.dagger_view_model", fileName())
            .addType(typeBuilder!!.build())
            .build()
            .writeTo(filer)
    }

    protected abstract fun fileName(): String
}
