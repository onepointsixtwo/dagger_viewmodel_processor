package com.onepointsixtwo.dagger_viewmodel_processor


import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

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
            .addModifiers(KModifier.PUBLIC)
            .addParameter(injecteeVariableName, javaObjectType)
            .addParameter(factoryVariableName, factoryType)
            .addAnnotation(Override::class.java)

        if (type == InjectViewModelProcessor.Type.FRAGMENT_TYPE && useActivityScope) {
            injectMethodBuilder.addStatement(
                "((\$T)\$L).\$L = \$T.of(((\$T)\$L).getActivity(), \$L).get(\$T.class)",
                enclosingClassType,
                injecteeVariableName,
                viewModelFieldInEnclosingClass.simpleName,
                viewModelProviders,
                enclosingClassType,
                injecteeVariableName,
                factoryVariableName,
                viewModelType
            )
        } else {
            injectMethodBuilder.addStatement(
                "((\$T)\$L).\$L = \$T.of((\$T)\$L, \$L).get(\$T.class)",
                enclosingClassType,
                injecteeVariableName,
                viewModelFieldInEnclosingClass.simpleName,
                viewModelProviders,
                enclosingClassType,
                injecteeVariableName,
                factoryVariableName,
                viewModelType
            )
        }

        val injectMethod = injectMethodBuilder.build()

        typeBuilder!!.addFunction(injectMethod)
    }

    internal override fun classType(): TypeName {
        return ClassName.bestGuess(name)
    }

    override fun fileName(): String {
        return name
    }
}
