package com.onepointsixtwo.dagger_viewmodel_processor


import com.squareup.kotlinpoet.*

import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * Creates a specific subclass of injector for injecting a specific activity or fragment
 */
class InjectorSubclass(
    enclosingClassType: TypeElement,
    private val baseInjectorClass: BaseInjectorClass,
    private val viewModelFieldInEnclosingClass: VariableElement,
    private val type: InjectViewModelProcessor.Type,
    private val useActivityScope: Boolean
) : BaseClass() {

    private val name: String
    protected override var typeBuilder: TypeSpec.Builder? = null
        private set
    private val enclosingClassType: TypeName

    init {

        this.enclosingClassType = ClassName.bestGuess(enclosingClassType.qualifiedName.toString())
        name = enclosingClassType.simpleName.toString() + "ViewModelInjector"

        createBasicClass()
        addInjectMethod()
    }

    private fun createBasicClass() {
        typeBuilder = TypeSpec.classBuilder(name)
            .superclass(baseInjectorClass.classType())
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
    }

    private fun addInjectMethod() {
        val viewModelType = ClassName.bestGuess(viewModelFieldInEnclosingClass.asType().toString())
        val viewModelProviders = ClassName.bestGuess("androidx.lifecycle.ViewModelProviders")

        val methodName = "inject"
        val injecteeVariableName = "injectee"
        val factoryVariableName = "factory"

        val injectMethodBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .addParameter(injecteeVariableName, javaObjectType)
            .addParameter(factoryVariableName, factoryType)
            .addAnnotation(Override::class.java)

        if (type == InjectViewModelProcessor.Type.FRAGMENT_TYPE && useActivityScope) {
            injectMethodBuilder.addStatement("(injectee as %T).%N = %T.of(injectee.activity!!, factory).get(%T::class.java)",
                enclosingClassType,
                viewModelFieldInEnclosingClass.simpleName,
                viewModelProviders,
                viewModelType)
        } else {
            injectMethodBuilder.addStatement("(injectee as %T).%N = %T.of(injectee, factory).get(%T::class.java)",
                enclosingClassType,
                viewModelFieldInEnclosingClass.simpleName,
                viewModelProviders,
                viewModelType)
        }

        val injectMethod = injectMethodBuilder.build()

        typeBuilder!!.addFunction(injectMethod)
    }

    internal override fun classType(): TypeName {
        return TypeVariableName(name)
    }

    override fun fileName(): String {
        return name
    }
}
