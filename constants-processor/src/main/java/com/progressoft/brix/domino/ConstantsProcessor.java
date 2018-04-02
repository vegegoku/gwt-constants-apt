package com.progressoft.brix.domino;

import com.google.auto.service.AutoService;
import com.google.gwt.i18n.client.LocalizableResource;
import com.progressoft.brix.domino.constants.Constants;
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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AutoService(Processor.class)
public class ConstantsProcessor extends AbstractProcessor {

    private Messager messager;
    private Types typeUtils;
    private Filer filer;
    private Elements elementUtils;
    private Type type;

    static String reduceLocale(String originalLocale) {
        if (isNull(originalLocale) || !originalLocale.contains("_"))
            return "";
        else
            return originalLocale.substring(0, originalLocale.indexOf("_"));
    }

    /**
     * Escapes string content to be a valid string literal.
     *
     * @return an escaped version of <code>unescaped</code>, suitable for being enclosed in double
     * quotes in Java source
     */
    public static String escape(String unescaped) {
        int extra = 0;
        for (int in = 0, n = unescaped.length(); in < n; ++in) {
            switch (unescaped.charAt(in)) {
                case '\0':
                case '\n':
                case '\r':
                case '\"':
                case '\\':
                    ++extra;
                    break;
            }
        }

        if (extra == 0) {
            return unescaped;
        }

        char[] oldChars = unescaped.toCharArray();
        char[] newChars = new char[oldChars.length + extra];
        for (int in = 0, out = 0, n = oldChars.length; in < n; ++in, ++out) {
            char c = oldChars[in];
            switch (c) {
                case '\0':
                    newChars[out++] = '\\';
                    c = '0';
                    break;
                case '\n':
                    newChars[out++] = '\\';
                    c = 'n';
                    break;
                case '\r':
                    newChars[out++] = '\\';
                    c = 'r';
                    break;
                case '\"':
                    newChars[out++] = '\\';
                    c = '"';
                    break;
                case '\\':
                    newChars[out++] = '\\';
                    c = '\\';
                    break;
            }
            newChars[out] = c;
        }

        return String.valueOf(newChars);
    }

    /**
     * Helper method used to wrap a string constant with quotes. Must use to
     * enable string escaping.
     *
     * @param wrapMe String to wrap
     * @return wrapped String
     */
    protected static String wrap(String wrapMe) {
        return "\"" + escape(wrapMe) + "\"";
    }

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
            constantsElements.stream().filter(e -> type.isAssignableFrom(e.asType(), com.google.gwt.i18n.client.Constants.class))
                    .forEach(this::generateConstants);
        } catch (Exception e) {
            new MessegerUtil(messager).handleError(e);
        }
        return true;
    }

    private void generateConstants(Element element) {

        String filename = element.getSimpleName().toString();
        String path = elementUtils.getPackageOf(element).toString()
                .replace('.', '/');

        Constants annotation = element.getAnnotation(Constants.class);

        LocaleInfo localeInfo = new LocaleInfo();
        localeInfo.locales = new HashSet<>(Arrays.asList(annotation.locales()));
        localeInfo.defaultLocale = annotation.defaultLocale();

        if (!localeInfo.locales.contains(localeInfo.defaultLocale))
            localeInfo.locales.add(localeInfo.defaultLocale);

        localeInfo.locales.forEach(locale -> {
            Properties properties = new Properties();
            try {

                FileObject fileObject = openPropertiesFile(path, filename, locale);

                if (nonNull(fileObject))
                    properties.load(new InputStreamReader(fileObject.openInputStream(), Charset.forName("UTF-8")));
                localeInfo.localesPropertiesMap.put(locale, properties);
            } catch (IOException ioe) {
                messager.printMessage(Diagnostic.Kind.ERROR, "could not load properties file [" + getPropertiesFileName(path, filename, locale) + "] for locale [" + locale + "]", element);
            }
        });

        if (!localeInfo.localesPropertiesMap.isEmpty()) {
            generateConstantsFiles(element, localeInfo);
        }
    }

    private FileObject openPropertiesFile(String path, String fileName, String locale) throws IOException {
        String propertiesFileName = getPropertiesFileName(path, fileName, locale);
        try {
            return processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "",
                    propertiesFileName);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.WARNING, "could not load properties file [" + propertiesFileName + "] for locale [" + locale + "]");
            if ("".equals(locale)) {
                throw e;
            } else {
                return openPropertiesFile(path, fileName, reduceLocale(locale));
            }
        }
    }

    private String getPropertiesFileName(String path, String fileName, String locale) {
        return path + "/" + fileName + ("".equals(locale) ? "" : "_") + locale + ".properties";
    }

    private void generateConstantsFiles(Element element, LocaleInfo localeInfo) {
        localeInfo.localesPropertiesMap.keySet().forEach(locale -> generateLocaleConstants(element, localeInfo, locale));
        new FactoryBuilder(elementUtils, filer, messager).generateStaticFactory(localeInfo, element);
    }

    private void generateLocaleConstants(Element element, LocaleInfo localeInfo, String locale) {
        String className = element.getSimpleName().toString() + "_" + locale;
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(TypeName.get(element.asType()));
        final boolean[] hasCache = new boolean[]{false};

        element.getEnclosedElements().stream().filter(e -> ElementKind.METHOD.equals(e.getKind()))
                .forEach(method -> {
                    ExecutableElement superMethod = (ExecutableElement) method;
                    Set<Modifier> modifiers = superMethod.getModifiers().stream().filter(modifier -> !(Modifier.ABSTRACT.equals(modifier) || Modifier.STATIC.equals(modifier))).collect(Collectors.toSet());

                    MethodContext methodContext = new MethodContext();
                    methodContext.locale = locale;
                    methodContext.localeInfo = localeInfo;
                    methodContext.methodName = superMethod.getSimpleName().toString();
                    methodContext.returnType = superMethod.getReturnType();

                    LocalizableResource.Key keyAnnotation = superMethod.getAnnotation(LocalizableResource.Key.class);
                    if (nonNull(keyAnnotation))
                        methodContext.propertyKey = keyAnnotation.value();
                    else
                        methodContext.propertyKey = methodContext.methodName;

                    MethodBlock methodBlock = createConstantMethod(superMethod, modifiers, methodContext);
                    if (methodBlock.needsCache && hasCache[0] == false) {
                        hasCache[0] = true;
                        builder.addField(FieldSpec.builder(TypeName.get(Map.class), "cache", Modifier.PRIVATE).initializer(CodeBlock.builder().addStatement("new $T()", HashMap.class).build()).build());
                    }
                    builder.addMethod(methodBlock.builder.build());
                });

        try {
            JavaFile.builder(elementUtils.getPackageOf(element).toString(), builder.build()).build().writeTo(filer);
        } catch (IOException e) {
            new MessegerUtil(messager).handleError(e);
        }
    }

    private MethodBlock createConstantMethod(ExecutableElement superMethod, Set<Modifier> modifiers, MethodContext methodContext) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodContext.methodName)
                .addAnnotation(Override.class)
                .addModifiers(modifiers)
                .addCode(getValueLiteral(methodContext))
                .returns(TypeName.get(superMethod.getReturnType()));
        MethodBlock methodBlock = new MethodBlock();
        methodBlock.builder = methodBuilder;
        methodBlock.needsCache = isNeedCache(superMethod.getReturnType());
        return methodBlock;
    }

    private boolean isNeedCache(TypeMirror returnType) {
        if ("java.lang.String[]".equals(returnType.toString())) {
            return true;
        } else if (isMap(returnType.toString())) {
            return true;
        }
        return false;
    }

    private CodeBlock getValueLiteral(MethodContext methodContext) {
        String returnTypeString = methodContext.returnType.toString();
        messager.printMessage(Diagnostic.Kind.WARNING, returnTypeString);
        if ("boolean".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $L", Boolean.valueOf(methodContext.getResource()).toString()).build();
        } else if ("int".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $L", methodContext.getResource()).build();
        } else if ("float".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $Lf", methodContext.getResource()).build();
        } else if ("double".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $L", methodContext.getResource()).build();
        } else if ("java.lang.String".equals(returnTypeString)) {
            return CodeBlock.builder().addStatement("return $L", "\"" + methodContext.getResource() + "\"").build();
        } else if ("java.lang.String[]".equals(returnTypeString)) {
            return arrayBlock(methodContext);
        } else if (isMap(returnTypeString)) {
            return mapBlock(methodContext);
        } else {
            return CodeBlock.builder().addStatement("return $L", methodContext.getResource()).build();
        }
    }

    private boolean isMap(String returnTypeString) {
        return "java.util.Map<java.lang.String,java.lang.String>".equals(returnTypeString) ||
                "java.util.Map".equals(returnTypeString);
    }

    private CodeBlock mapBlock(MethodContext methodContext) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$1T<$2T,$2T> args = ($1T<$2T,$2T>) cache.get(\"$3L\")", Map.class, String.class, methodContext.propertyKey)
                .beginControlFlow("if (args == null)")
                .addStatement("args = new $1T<$2T,$2T>()", LinkedHashMap.class, String.class);
        if (!"".equals(methodContext.getResource()) && nonNull(methodContext.getResource()))
            Arrays.stream(split(methodContext.getResource()))
                    .forEach(key -> {
                        String value = methodContext.localeInfo.localesPropertiesMap.get(methodContext.locale).getProperty(key);
                        builder.addStatement("args.put(" + wrap(key) + "," + wrap(value) + ")");
                    });
        builder.addStatement("args = $T.unmodifiableMap(args)", Collections.class);
        builder.addStatement("cache.put(" + wrap(methodContext.propertyKey) + ", args)");

        builder.endControlFlow()
                .addStatement("return args")
                .build();


        return builder.build();
    }

    private CodeBlock arrayBlock(MethodContext methodContext) {
        return CodeBlock.builder()
                .addStatement("$T args[] = ($T[]) cache.get(\"$L\")", String.class, String.class, methodContext.propertyKey)
                .beginControlFlow("if (args == null)")
                .addStatement("$T[] writer = {\n$L\n}", String.class, asStringArrayElements(methodContext.getResource()))
                .addStatement("cache.put(\"stringArray\", writer)")
                .addStatement("return writer")
                .nextControlFlow("else")
                .addStatement("return args")
                .endControlFlow().build();
    }

    String[] split(String target) {
        // We add an artificial end character to avoid the odd split() behavior
        // that drops the last item if it is only whitespace.
        target = target + "~";

        // Do not split on escaped commas.
        String[] args = target.split("(?<![\\\\]),");

        // Now remove the artificial ending we added above.
        // We have to do it before we escape and trim because otherwise
        // the artificial trailing '~' would prevent the last item from being
        // properly trimmed.
        if (args.length > 0) {
            int last = args.length - 1;
            args[last] = args[last].substring(0, args[last].length() - 1);
        }

        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].replaceAll("\\\\,", ",").trim();
        }
        return args;
    }

    private String asStringArrayElements(String propertyValue) {
        return Arrays.stream(split(propertyValue)).map(s -> (wrap(s))).collect(Collectors.joining(",\n"));
    }

    class LocaleInfo {
        HashSet<String> locales;
        String defaultLocale;
        Map<String, Properties> localesPropertiesMap = new HashMap<>();

        String getResource(String key, String locale) {
            if (!localesPropertiesMap.containsKey(locale)) {
                ConstantsProcessor.this.messager.printMessage(Diagnostic.Kind.ERROR, "Missing resource for key'" + key + "'");
                throw new MissingResourceException("Missing resource for key'" + key + "'");
            }
            if ("".equals(locale)) {
                if (localesPropertiesMap.get(defaultLocale).containsKey(key)) {
                    return localesPropertiesMap.get(defaultLocale).getProperty(key);
                } else {
                    ConstantsProcessor.this.messager.printMessage(Diagnostic.Kind.ERROR, "Missing resource for key'" + key + "'");
                    throw new MissingResourceException("Missing resource for key'" + key + "'");
                }
            }
            if (localesPropertiesMap.get(locale).containsKey(key))
                return localesPropertiesMap.get(locale).getProperty(key);
            else
                return getResource(key, reduceLocale(locale));
        }
    }

    class MethodContext {
        String locale;
        String propertyKey;
        String methodName;
        LocaleInfo localeInfo;
        TypeMirror returnType;

        public String getResource() {
            return localeInfo.getResource(propertyKey, locale);
        }
    }

    class MethodBlock {
        MethodSpec.Builder builder;
        boolean needsCache = false;
    }

    private class MissingResourceException extends RuntimeException {
        public MissingResourceException(String message) {
            super(message);
        }
    }
}
