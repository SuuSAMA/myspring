package com.leonardo.myspring.core;


public class MyApplicationRunner {

    public static ApplicationContext run(){
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.refresh();
        return applicationContext;
    }
}
