package com.leonardo.myspring.annotation;

import java.lang.annotation.*;



@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {
}
