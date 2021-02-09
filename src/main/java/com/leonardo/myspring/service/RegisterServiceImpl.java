package com.leonardo.myspring.service;

import com.leonardo.myspring.annotation.Autowire;
import com.leonardo.myspring.annotation.Component;
import com.leonardo.myspring.bean.Person;
import com.leonardo.myspring.bean.ProductA;


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
