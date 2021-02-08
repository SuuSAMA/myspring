package com.leonardo.myspring.core;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/5
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/5
 */
public class MyApplicationRunner {

    public static ApplicationContext run(){
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.refresh();
        return applicationContext;
    }
}
