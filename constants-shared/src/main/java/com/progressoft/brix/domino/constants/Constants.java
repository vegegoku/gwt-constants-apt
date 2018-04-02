package com.progressoft.brix.domino.constants;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Constants {
    String[] locales() default {};

    String defaultLocale() default "";
}
