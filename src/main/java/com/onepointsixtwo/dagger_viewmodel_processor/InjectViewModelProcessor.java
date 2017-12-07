package com.onepointsixtwo.dagger_viewmodel_processor;


import com.onepointsixtwo.dagger_viewmodel.InjectViewModel;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.onepointsixtwo.dagger_viewmodel.InjectViewModel")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class InjectViewModelProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            tryToProcessAnnotations(set, roundEnvironment);
        } catch (Exception ex) {}
        return true;
    }

    private void tryToProcessAnnotations(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
            throws Exception {
        /**
         * Create the base injector class from which the injectors for a specific fragment or activity inherit.
         * Only has one abstract method inject(Object object, ViewModelProvider.Factory factory)
         */
        BaseInjectorClass baseInjector = new BaseInjectorClass();
        baseInjector.outputClassToFiler(filer);

        HashMap<TypeName, TypeName> fragmentOrActivityToInjectorMapping = new HashMap<>();

        //Iterate over all elements with the annotation.
        for (Element element : roundEnvironment.getElementsAnnotatedWith(InjectViewModel.class)) {

            //If the element's type is a field we're all good, otherwise return
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Only fields can be injected using InjectViewModel");
                return;
            }

            //Get VariableElement (the var holding the ViewModel) and the enclosing class's type
            VariableElement viewModelField = (VariableElement) element;
            TypeElement enclosingClassType = (TypeElement) element.getEnclosingElement();

            //Get the annotation itself (so we can retrieve the value of whether we should use activity scope rather
            //than fragment for getting the view model) and also the type. We want the type to a) check it's a legit value
            //and b) to (if it's a fragment) use the activity if the annotation's value is true.
            InjectViewModel annotation = element.getAnnotation(InjectViewModel.class);
            Type type = checkEnclosingTypeIsFragmentOrActivitySubclass(enclosingClassType);

            //Create the class to inject the ViewModel for the given enclosing type
            InjectorSubclass subclass = new InjectorSubclass(enclosingClassType, baseInjector, viewModelField, type,
                    annotation.useActivityScope());
            subclass.outputClassToFiler(filer);

            //Add to the mapping which will be used in the ViewModelInjectors class to map an injector to a class it
            //is able to inject.
            fragmentOrActivityToInjectorMapping.put(ClassName.get(enclosingClassType), subclass.classType());
        }

        //If the mapping is _not_ empty, then we should create the view model injectors class.
        if (!fragmentOrActivityToInjectorMapping.isEmpty()) {
            ViewModelInjectorsClass viewModelInjectorsClass =
                    new ViewModelInjectorsClass(fragmentOrActivityToInjectorMapping, baseInjector);
            viewModelInjectorsClass.outputClassToFiler(filer);
        }
    }

    private Type checkEnclosingTypeIsFragmentOrActivitySubclass(TypeElement type) throws Exception {
        if (type.getSimpleName().toString().equals("Activity") ||
                type.getSimpleName().toString().equals("ActivityCompat")) {
            return Type.ACTIVITY_TYPE;
        } else if (type.getSimpleName().toString().equals("Fragment")) {
            return Type.FRAGMENT_TYPE;
        }

        TypeMirror superclasstypeMirror = type.getSuperclass();
        TypeElement superclassTypeElement = (TypeElement) ((DeclaredType) superclasstypeMirror).asElement();
        if (superclassTypeElement != null) {
            return checkEnclosingTypeIsFragmentOrActivitySubclass(superclassTypeElement);
        }

        throw new Exception();
    }

    enum Type {
        FRAGMENT_TYPE,
        ACTIVITY_TYPE
    }
}
