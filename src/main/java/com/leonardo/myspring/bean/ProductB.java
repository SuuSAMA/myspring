package com.leonardo.myspring.bean;

import com.leonardo.myspring.annotation.Autowire;
import com.leonardo.myspring.annotation.Component;


@Component
public class ProductB {
    @Autowire
    private ProductA productA;
}
