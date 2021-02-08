package com.leonardo.myspring;

import com.leonardo.myspring.annotation.Autowire;
import com.leonardo.myspring.annotation.Component;
import com.leonardo.myspring.core.ApplicationContext;
import com.leonardo.myspring.core.MyApplicationRunner;
import com.leonardo.myspring.service.RegisterService;
import com.leonardo.myspring.service.RegisterServiceImpl;
import org.junit.Test;

import javax.annotation.PostConstruct;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/5
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/5
 */
@Component
public class MySpringApplication {

    public static void main(String[] args) {
        ApplicationContext application = MyApplicationRunner.run();
        RegisterService bean = application.getBean(RegisterService.class);
        bean.printPerson();
    }

}
