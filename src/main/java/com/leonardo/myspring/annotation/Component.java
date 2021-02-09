package com.leonardo.myspring.annotation;

import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}
