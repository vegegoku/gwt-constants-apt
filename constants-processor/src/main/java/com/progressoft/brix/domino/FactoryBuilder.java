package com.progressoft.brix.domino;

import com.progressoft.brix.domino.constants.CouldNotLoadConstantsException;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.io.IOException;

public class FactoryBuilder {

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    public FactoryBuilder(Elements elementUtils, Filer filer, Messager messager) {
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.messager = messager;
    }

    void generateStaticFactory(ConstantsProcessor.LocaleInfo localeInfo, Element element) {

        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.get(element.asType()));
        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        localeInfo.localesPropertiesMap.keySet().forEach(locale -> {
            if (!"".equals(locale)) {
                codeBuilder
                        .beginControlFlow("if(\"" + locale + "\".equals(System.getProperty(\"locale\")))")
                        .addStatement("return new $T()", ClassName.bestGuess(elementUtils.getPackageOf(element).toString() +
                                "." + element.getSimpleName().toString() + "_" + locale))
                        .endControlFlow();
            }
        });
        codeBuilder
                .beginControlFlow("if(\"default\".equals(System.getProperty(\"locale\")))")
                .addStatement("return new $T()", ClassName.bestGuess(elementUtils.getPackageOf(element).toString() +
                        "." + element.getSimpleName().toString() + "_" + localeInfo.defaultLocale))
                .endControlFlow();

        codeBuilder.addStatement("throw new $T($L)", CouldNotLoadConstantsException.class, "\"No matching implementation for " +
                "[\"+System.getProperty(\"locale\")+\"] found\"");

        createMethod.addCode(codeBuilder.build());

        TypeSpec.Builder builder = TypeSpec.classBuilder(element.getSimpleName().toString() + "_factory")
                .addMethod(createMethod.build());
        try {
            JavaFile.builder(elementUtils.getPackageOf(element).toString(), builder.build()).build().writeTo(filer);
        } catch (IOException e) {
            new MessegerUtil(messager).handleError(e);
        }

    }
}