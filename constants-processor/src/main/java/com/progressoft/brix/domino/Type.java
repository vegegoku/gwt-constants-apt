package com.progressoft.brix.domino;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class Type {

    private final Types typeUtils;
    private final Elements elementUtils;

    public Type(Types typeUtils, Elements elementUtils) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
    }

    public boolean isAssignableFrom(TypeMirror typeMirror, Class<?> targetClass) {
        return typeUtils.isAssignable(typeMirror, typeUtils.getDeclaredType(elementUtils.getTypeElement(targetClass.getName())));
    }

    public boolean isPrimitive(TypeMirror typeMirror) {
        return typeMirror.getKind().isPrimitive();
    }
}
