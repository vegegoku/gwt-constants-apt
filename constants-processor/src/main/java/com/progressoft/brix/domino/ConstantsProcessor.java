package com.progressoft.brix.domino;

import com.google.auto.service.AutoService;
import com.progressoft.brix.domino.constants.Constants;
import com.progressoft.brix.domino.constants.CouldNotLoadConstantsException;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@AutoService(Processor.class)
public class ConstantsProcessor extends AbstractProcessor {

    private Messager messager;
    private Types typeUtils;
    private Filer filer;
    private Elements elementUtils;
    private Type type;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Stream
                .of(Constants.class)
                .map(Class::getCanonicalName).collect(Collectors.toSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.type = new Type(typeUtils, elementUtils);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<? extends Element> constantsElements = roundEnv.getElementsAnnotatedWith(Constants.class);
            constantsElements.stream().filter(e -> type.isAssignableFrom(e.asType(), com.google.gwt.i18n.client.Constants.class)).forEach(this::generateConstants);
        } catch (Exception e) {
            handleError(e);
        }
        return true;
    }

    private void generateConstants(Element element) {

        Constants annotation = element.getAnnotation(Constants.class);
        String[] locales = annotation.locales();
        String filename = element.getSimpleName().toString();

        Map<String, Properties> localePropertiesMap = new HashMap<>();

        Arrays.stream(locales).forEach(l -> {
            Properties properties = new Properties();
            try {
                FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "",
                        elementUtils.getPackageOf(element).toString().replace('.', '/') + "/" + filename + "_" + l + ".properties");

                if (nonNull(fileObject))
                    properties.load(fileObject.openInputStream());
                localePropertiesMap.put(l, properties);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "could not load properties file for locale [" + l + "]", element);
            }
        });

        if (!localePropertiesMap.isEmpty()) {
            generateConstantsFiles(element, localePropertiesMap);
        }

    }

    private void generateConstantsFiles(Element element, Map<String, Properties> localePropertiesMap) {
        localePropertiesMap.keySet().forEach(locale -> {
            String className = element.getSimpleName().toString() + "_" + locale;
            TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(TypeName.get(element.asType()));

            element.getEnclosedElements().stream().filter(e -> ElementKind.METHOD.equals(e.getKind()))
                    .forEach(m -> {
                        ExecutableElement superMethod = (ExecutableElement) m;
                        Set<Modifier> modifiers = superMethod.getModifiers().stream().filter(modifier -> !(Modifier.ABSTRACT.equals(modifier) || Modifier.STATIC.equals(modifier))).collect(Collectors.toSet());
                        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(superMethod.getSimpleName().toString())
                                .addAnnotation(Override.class)
                                .addModifiers(modifiers)
                                .addCode( getValueLiteral(localePropertiesMap.get(locale).getProperty(superMethod.getSimpleName().toString()), superMethod.getReturnType(), superMethod.getSimpleName().toString()))
                                .returns(TypeName.get(superMethod.getReturnType()));
                        if("java.lang.String[]".equals(superMethod.getReturnType().toString())){
                            builder.addField(FieldSpec.builder(TypeName.get(Map.class), "cache", Modifier.PRIVATE).initializer(CodeBlock.builder().addStatement("new $T()", HashMap.class).build()).build());
                        }
                        builder.addMethod(methodBuilder.build());
                    });

            try {
                JavaFile.builder(elementUtils.getPackageOf(element).toString(), builder.build()).build().writeTo(filer);
            } catch (IOException e) {
                handleError(e);
            }
        });

        generateStaticFactory(localePropertiesMap, element);
    }

    private void generateStaticFactory(Map<String, Properties> localePropertiesMap, Element element) {

        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.STATIC)
                .returns(TypeName.get(element.asType()));
        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        localePropertiesMap.keySet().forEach(locale->{
            codeBuilder
                    .beginControlFlow("if(\""+locale+"\".equals(System.getProperty(\"locale\")))")
                    .addStatement("return new $T()", ClassName.bestGuess(elementUtils.getPackageOf(element).toString()+"."+element.getSimpleName().toString()+"_"+locale))
                    .endControlFlow();
        });
        Constants constantsAnnotation = element.getAnnotation(Constants.class);
        if(nonNull(constantsAnnotation.defaultLocale()) && !constantsAnnotation.defaultLocale().isEmpty()){
            codeBuilder
                    .beginControlFlow("if(\"default\".equals(System.getProperty(\"locale\")))")
                    .addStatement("return new $T()", ClassName.bestGuess(elementUtils.getPackageOf(element).toString()+"."+element.getSimpleName().toString()+"_"+constantsAnnotation.defaultLocale()))
                    .endControlFlow();
        }

        codeBuilder.addStatement("throw new $T($L)", CouldNotLoadConstantsException.class, "\"No matching implementation for [\"+System.getProperty(\"locale\")+\"] found\"");

        createMethod.addCode(codeBuilder.build());

        TypeSpec.Builder builder = TypeSpec.classBuilder(element.getSimpleName().toString()+"_factory")
                .addMethod(createMethod.build());
        try {
            JavaFile.builder(elementUtils.getPackageOf(element).toString(), builder.build()).build().writeTo(filer);
        } catch (IOException e) {
            handleError(e);
        }

    }

    private CodeBlock getValueLiteral(String propertyValue, TypeMirror returnType, String propertyKey) {
        String returnTypeString = returnType.toString();
        if ("boolean".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $L", Boolean.valueOf(propertyValue).toString()).build();
        } else if ("int".equals(returnTypeString) || "float".equals(returnTypeString) || "double".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $L", propertyValue).build();
        } else if ("java.lang.String".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $L", "\"" + propertyValue + "\"").build();
        } else if("java.lang.String[]".equals(returnTypeString)){
            return CodeBlock.builder()
                    .addStatement("$T args[] = ($T[]) cache.get(\"$L\")", String.class, String.class, propertyKey)
                    .beginControlFlow("if (args == null)")
                    .addStatement("$T[] writer = {$L}", String.class,  asStringArrayElements(propertyValue))
                    .addStatement("cache.put(\"stringArray\", writer)")
                    .addStatement("return writer")
                    .nextControlFlow("else")
                    .addStatement("return args")
                    .endControlFlow().build();
        }else{
            return CodeBlock.builder().addStatement("return $L", propertyValue).build();
        }
    }

    private String asStringArrayElements(String propertyValue) {
        return Arrays.stream(propertyValue.split(",")).map(s -> ("\"" + s + "\"")).collect(Collectors.joining(","));
    }

    private void handleError(Exception e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        messager.printMessage(Diagnostic.Kind.ERROR, "error while creating source file " + out.getBuffer().toString());
    }
}
