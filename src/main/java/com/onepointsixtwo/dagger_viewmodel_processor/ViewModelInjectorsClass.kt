package com.onepointsixtwo.dagger_viewmodel_processor


import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

import java.util.HashMap

class ViewModelInjectorsClass(
    classToInjectorTypeMapping: HashMap<TypeName, TypeName>,
    private val baseInjectorClass: BaseInjectorClass
) : BaseClass() {

    protected override var typeBuilder: TypeSpec.Builder? = null
        private set
    private val mapVariableName = "map"
    private val supportActivityType = ClassName.bestGuess("androidx.core.app.ActivityCompat")
    private val supportFragmentType = ClassName.bestGuess("androidx.fragment.app.Fragment")
    private val activityType = ClassName.bestGuess("android.app.Activity")
    private val fragmentType = ClassName.bestGuess("android.app.Fragment")

    init {
        createBasicClass()
        createGetHashmapOfClassToInjectorMethod(classToInjectorTypeMapping)
        createInjectClassActivityMethod()
        createInjectClassFragmentMethod()
        createInjectClassSupportActivityMethod()
        createInjectClassSupportFragmentMethod()
    }

    private fun createBasicClass() {
        typeBuilder = TypeSpec.objectBuilder("ViewModelInjectors")
            .addModifiers(KModifier.PUBLIC)
    }

    /**
     * Creates a method on this class to build a hashmap of the class an injector can inject, to the injector itself mapping
     */
    private fun createGetHashmapOfClassToInjectorMethod(classToInjectorTypeMapping: HashMap<TypeName, TypeName>) {
        val getInjectorsMethod = FunSpec.builder("getInjectorsForClass")
            .addModifiers(KModifier.PRIVATE)

        //Create the hashmap
        val mapVariableName = "map"
        getInjectorsMethod.addStatement(
            "val %N = mutableMapOf<%T<*>, %T>()",
            mapVariableName,
            classTypeName,
            baseInjectorClass.classType()
        )

        //Add the values to it - the class maps to the instance of the injector for it
        for ((fragmentOrActivityType, injectorType) in classToInjectorTypeMapping) {
            getInjectorsMethod
                .addStatement(
                    "%N.put(%T::class, %T())",
                    mapVariableName,
                    fragmentOrActivityType,
                    injectorType
                )
        }

        //Handle return statement to return created hashmap
        getInjectorsMethod.addStatement("return %N", mapVariableName)

        val returnType = Map::class.asClassName().parameterizedBy(classTypeName, baseInjectorClass.classType())
        getInjectorsMethod.returns(returnType)

        //Add the method
        typeBuilder!!.addFunction(getInjectorsMethod.build())
    }

    private fun createInjectClassFragmentMethod() {
        val injecteeParamName = "fragment"
        val factoryParamName = "factory"
        val injectorVariableName = "injector"

        val inject = FunSpec.builder("inject")
        inject.addModifiers(KModifier.PUBLIC)

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(injecteeParamName, fragmentType)
            .addParameter(factoryParamName, factoryType)

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement(
            "\$T<\$T, \$T> \$L = getInjectorsForClass()",
            hashmapClassName,
            classTypeName,
            baseInjectorClass.classType(),
            mapVariableName
        )
        inject.addStatement(
            "\$T \$L = \$L.get(\$L.getClass())",
            baseInjectorClass.classType(),
            injectorVariableName,
            mapVariableName,
            injecteeParamName
        )
        inject.addStatement(
            "if (\$L != null) { \$L.inject(\$L, \$L); }",
            injectorVariableName,
            injectorVariableName,
            injecteeParamName,
            "factory"
        )

        typeBuilder!!.addFunction(inject.build())
    }

    private fun createInjectClassActivityMethod() {
        val injecteeParamName = "activity"
        val factoryParamName = "factory"
        val injectorVariableName = "injector"

        val inject = FunSpec.builder("inject")
        inject.addModifiers(KModifier.PUBLIC)

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(injecteeParamName, activityType)
            .addParameter(factoryParamName, factoryType)

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement(
            "\$T<\$T, \$T> \$L = getInjectorsForClass()",
            hashmapClassName,
            classTypeName,
            baseInjectorClass.classType(),
            mapVariableName
        )
        inject.addStatement(
            "\$T \$L = \$L.get(\$L.getClass())",
            baseInjectorClass.classType(),
            injectorVariableName,
            mapVariableName,
            injecteeParamName
        )
        inject.addStatement(
            "if (\$L != null) { \$L.inject(\$L, \$L); }",
            injectorVariableName,
            injectorVariableName,
            injecteeParamName,
            "factory"
        )

        typeBuilder!!.addFunction(inject.build())
    }

    private fun createInjectClassSupportFragmentMethod() {
        val injecteeParamName = "supportFragment"
        val factoryParamName = "factory"
        val injectorVariableName = "injector"

        val inject = FunSpec.builder("inject")
        inject.addModifiers(KModifier.PUBLIC, KModifier.COMPANION)

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(injecteeParamName, supportFragmentType)
            .addParameter(factoryParamName, factoryType)

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement(
            "\$T<\$T, \$T> \$L = getInjectorsForClass()",
            hashmapClassName,
            classTypeName,
            baseInjectorClass.classType(),
            mapVariableName
        )
        inject.addStatement(
            "\$T \$L = \$L.get(\$L.getClass())",
            baseInjectorClass.classType(),
            injectorVariableName,
            mapVariableName,
            injecteeParamName
        )
        inject.addStatement(
            "if (\$L != null) { \$L.inject(\$L, \$L); }",
            injectorVariableName,
            injectorVariableName,
            injecteeParamName,
            "factory"
        )

        typeBuilder!!.addFunction(inject.build())
    }

    private fun createInjectClassSupportActivityMethod() {
        val injecteeParamName = "supportActivity"
        val factoryParamName = "factory"
        val injectorVariableName = "injector"

        val inject = FunSpec.builder("inject")
        inject.addModifiers(KModifier.PUBLIC)

        //Input parameters of an object type and the factory to build view models
        inject.addParameter(injecteeParamName, supportActivityType)
            .addParameter(factoryParamName, factoryType)

        //Gets the injector for the given class, checks it is not null, and if it's OK, uses it to inject the class.
        inject.addStatement(
            "\$T<\$T, \$T> \$L = getInjectorsForClass()",
            hashmapClassName,
            classTypeName,
            baseInjectorClass.classType(),
            mapVariableName
        )
        inject.addStatement(
            "\$T \$L = \$L.get(\$L.getClass())",
            baseInjectorClass.classType(),
            injectorVariableName,
            mapVariableName,
            injecteeParamName
        )
        inject.addStatement(
            "if (\$L != null) { \$L.inject(\$L, \$L); }",
            injectorVariableName,
            injectorVariableName,
            injecteeParamName,
            "factory"
        )

        typeBuilder!!.addFunction(inject.build())
    }


    internal override fun classType(): TypeName {
        return ClassName.bestGuess(name)
    }

    override fun fileName(): String {
        return name
    }

    companion object {

        private val name = "ViewModelInjectors"
    }
}
