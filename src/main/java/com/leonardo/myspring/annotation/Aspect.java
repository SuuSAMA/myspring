package com.leonardo.myspring.annotation;

import java.lang.annotation.*;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/7
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/7
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {
}
