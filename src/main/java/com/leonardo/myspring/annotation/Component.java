package com.leonardo.myspring.annotation;

import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.annotation.*;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/5
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/5
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}
