package com.leonardo.myspring.core;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/7
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/7
 */
public abstract class BeanMethodAdvance implements MethodInterceptor {

    private Object target;

    private String proxyMethodName;

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        doBefore();

        Object result = methodProxy.invokeSuper(o, objects);

        doAfterReturning();

        return result;
    }

    public Object createProxyObject(Object target){
        this.target = target;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.target.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    public abstract void doBefore();

    public abstract void doAfterReturning();

}
