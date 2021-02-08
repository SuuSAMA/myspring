package com.leonardo.myspring.service;

import com.leonardo.myspring.annotation.Autowire;
import com.leonardo.myspring.annotation.Component;
import com.leonardo.myspring.bean.Person;
import com.leonardo.myspring.bean.ProductA;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/5
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/5
 */
@Component
public class RegisterServiceImpl implements RegisterService {
    @Autowire
    private Person person;

    @Autowire
    private ProductA product;

    @Override
    public void printPerson(){
        System.out.println("hello, " + person.getName());
    }
}
