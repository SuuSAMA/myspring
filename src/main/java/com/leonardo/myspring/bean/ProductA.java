package com.leonardo.myspring.bean;

import com.leonardo.myspring.annotation.Autowire;
import com.leonardo.myspring.annotation.Component;


@Component
public class ProductA {
    @Autowire
    private ProductB productB;
}
