package com.onepointsixtwo.dagger_viewmodel_processor


import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.IOException
import javax.annotation.processing.Filer
import kotlin.reflect.KClass

abstract class BaseClass {

    protected var factoryType: TypeName =
        ClassName.bestGuess("androidx.lifecycle.ViewModelProvider.Factory")
    protected var javaObjectType: TypeName = ClassName.bestGuess("kotlin.Any")
    protected var hashmapClassName: TypeName = ClassName.bestGuess("java.util.HashMap")
    protected var classTypeName: TypeName =
        KClass::class.asTypeName().parameterizedBy(TypeVariableName("*"))

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
