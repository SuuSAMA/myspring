package com.leonardo.myspring.aspect;

import com.leonardo.myspring.annotation.*;
import com.leonardo.myspring.core.BeanMethodAdvance;
import com.leonardo.myspring.core.DefaultBeanMethodAdvance;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/7
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/7
 */
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
