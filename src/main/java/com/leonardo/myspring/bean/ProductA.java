package com.leonardo.myspring.bean;

import com.leonardo.myspring.annotation.Autowire;
import com.leonardo.myspring.annotation.Component;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/7
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/7
 */
@Component
public class ProductA {
    @Autowire
    private ProductB productB;
}
