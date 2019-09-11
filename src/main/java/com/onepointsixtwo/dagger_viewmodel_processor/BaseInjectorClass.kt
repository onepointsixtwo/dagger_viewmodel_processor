package com.onepointsixtwo.dagger_viewmodel_processor


import com.squareup.kotlinpoet.*

/**
 * Creator for the base injector class.
 * Not particularly type-safe, but it basically downcasts to Object whatever the input is so it can be upcast to
 * Activity or Fragment to do the injection with the Factory.
 *
 * TODO: work out how to verify if a class is a subclass of fragment or activity for type-safety.
 */
class BaseInjectorClass : BaseClass() {
    private val name = "BaseViewModelInjector"
    protected override var typeBuilder: TypeSpec.Builder? = null
        private set

    init {
        createBasicClass()
        addAbstractInjectionMethod()
    }

    private fun createBasicClass() {
        typeBuilder = TypeSpec.classBuilder("BaseViewModelInjector")
            .addModifiers(KModifier.ABSTRACT)
    }

    private fun addAbstractInjectionMethod() {
        val superclassMethod = FunSpec.builder("inject")
            .addParameter("injectee", javaObjectType)
            .addParameter("factory", factoryType)
            .addModifiers(KModifier.ABSTRACT).build()
        typeBuilder!!.addFunction(superclassMethod)
    }

    internal override fun classType(): TypeName {
        return TypeVariableName(name)
    }

    override fun fileName(): String {
        return name
    }
}
