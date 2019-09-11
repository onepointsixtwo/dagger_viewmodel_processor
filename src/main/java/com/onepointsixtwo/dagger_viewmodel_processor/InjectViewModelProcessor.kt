package com.onepointsixtwo.dagger_viewmodel_processor


import com.google.auto.service.AutoService
import com.onepointsixtwo.dagger_viewmodel.InjectViewModel
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

@AutoService(Processor::class)
class InjectViewModelProcessor : AbstractProcessor() {

    private var filer: Filer? = null
    private var messager: Messager? = null

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(InjectViewModel::class.java.name)
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        try {
            tryToProcessAnnotations(set, roundEnvironment)
        } catch (ex: Exception) {
        }

        return true
    }

    @Throws(Exception::class)
    private fun tryToProcessAnnotations(set: Set<TypeElement>, roundEnvironment: RoundEnvironment) {
        /**
         * Create the base injector class from which the injectors for a specific fragment or activity inherit.
         * Only has one abstract method inject(Object object, ViewModelProvider.Factory factory)
         */
        val baseInjector = BaseInjectorClass()
        baseInjector.outputClassToFiler(filer!!)

        val fragmentOrActivityToInjectorMapping = HashMap<TypeName, TypeName>()

        //Iterate over all elements with the annotation.
        for (element in roundEnvironment.getElementsAnnotatedWith(InjectViewModel::class.java)) {

            //If the element's type is a field we're all good, otherwise return
            if (element.kind != ElementKind.FIELD) {
                messager!!.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Only fields can be injected using InjectViewModel"
                )
                return
            }

            //Get VariableElement (the var holding the ViewModel) and the enclosing class's type
            val viewModelField = element as VariableElement
            val enclosingClassType = element.getEnclosingElement() as TypeElement

            //Get the annotation itself (so we can retrieve the value of whether we should use activity scope rather
            //than fragment for getting the view model) and also the type. We want the type to a) check it's a legit value
            //and b) to (if it's a fragment) use the activity if the annotation's value is true.
            val annotation = element.getAnnotation(InjectViewModel::class.java)
            val type = checkEnclosingTypeIsFragmentOrActivitySubclass(enclosingClassType)

            //Create the class to inject the ViewModel for the given enclosing type
            val subclass = InjectorSubclass(
                enclosingClassType, baseInjector, viewModelField, type,
                annotation.useActivityScope
            )
            subclass.outputClassToFiler(filer!!)

            //Add to the mapping which will be used in the ViewModelInjectors class to map an injector to a class it
            //is able to inject.
            fragmentOrActivityToInjectorMapping[ClassName.bestGuess(enclosingClassType.qualifiedName.toString())] =
                subclass.classType()
        }

        //If the mapping is _not_ empty, then we should create the view model injectors class.
        if (!fragmentOrActivityToInjectorMapping.isEmpty()) {
            val viewModelInjectorsClass =
                ViewModelInjectorsClass(fragmentOrActivityToInjectorMapping, baseInjector)
            viewModelInjectorsClass.outputClassToFiler(filer!!)
        }
    }

    @Throws(Exception::class)
    private fun checkEnclosingTypeIsFragmentOrActivitySubclass(type: TypeElement): Type {
        if (type.simpleName.toString() == "Activity" || type.simpleName.toString() == "ActivityCompat") {
            return Type.ACTIVITY_TYPE
        } else if (type.simpleName.toString() == "Fragment") {
            return Type.FRAGMENT_TYPE
        }

        val superclasstypeMirror = type.superclass
        val superclassTypeElement =
            (superclasstypeMirror as DeclaredType).asElement() as TypeElement
        if (superclassTypeElement != null) {
            return checkEnclosingTypeIsFragmentOrActivitySubclass(superclassTypeElement)
        }

        throw Exception()
    }

    enum class Type {
        FRAGMENT_TYPE,
        ACTIVITY_TYPE
    }
}
