package com.leonardo.myspring.aspect;

import com.leonardo.myspring.annotation.*;
import com.leonardo.myspring.core.BeanMethodAdvance;
import com.leonardo.myspring.core.DefaultBeanMethodAdvance;


@Aspect
@Component
public class RegisterAspect extends DefaultBeanMethodAdvance {

    @PointCut("com.leonardo.myspring.service.RegisterServiceImpl")
    public void pointCut(){}

    @Override
    public void doBefore(){
        System.out.println("前置切面");
    }

    @Override
    public void doAfterReturning(){
        System.out.println("后置切面");
    }

}
