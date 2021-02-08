package com.leonardo.myspring.core;

import com.leonardo.myspring.annotation.*;
import com.leonardo.myspring.cost.CustomConstant;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/5
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/5
 */
public class ApplicationContext {
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(16);
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(16);
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    private final Map<String, BeanMethodAdvance> aopMap = new ConcurrentHashMap<>(16);

    private final static String DEFAULT_ASPECT = "defaultAspect";

    private Logger log = LoggerFactory.getLogger(ApplicationContext.class);
    public void refresh(){
        registerBeanPostProcessors();
        registerBeanDefinition();
        initSingleBean();
        System.out.println();
    }

    private void registerBeanDefinition(){
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);
        for (Class<?> clazz: classes){
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setName(clazz.getName());
            beanDefinition.setClazz(clazz);
            beanDefinition.setClassPath(clazz.getPackage().getName());

            Component component = clazz.getAnnotation(Component.class);
            String key = clazz.getName();
            if (StringUtils.isNotBlank(component.value())){
                key = component.value();
            }

            beanDefinitionMap.put(key, beanDefinition);
        }
    }

    private void registerBeanPostProcessors() {
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(Aspect.class);
        for (Class<?> type : types) {
            PointCut pointCut = null;

            Method[] methods = type.getDeclaredMethods();
            for (Method method: methods){
                if (method.isAnnotationPresent(PointCut.class)){
                    pointCut = method.getAnnotation(PointCut.class);
                }
            }

            if (pointCut != null){
                String beanName = pointCut.value();

                try {
                    BeanMethodAdvance beanMethodAdvance = (BeanMethodAdvance) type.newInstance();
                    aopMap.put(beanName, beanMethodAdvance);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

        Class<?> defaultAspect = reflections.getTypesAnnotatedWith(DefaultAspect.class).iterator().next();

        try {
            BeanMethodAdvance beanMethodAdvance = (BeanMethodAdvance) defaultAspect.newInstance();
            aopMap.put(DEFAULT_ASPECT, beanMethodAdvance);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void initSingleBean(){
        try {
            for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
                createBean(entry.getKey());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Object initializeBean(String beanName, Object o){
        BeanMethodAdvance methodAdvance = null;

        if (aopMap.containsKey(beanName)){
            methodAdvance = aopMap.get(beanName);
        } else {
            methodAdvance = aopMap.get(DEFAULT_ASPECT);
        }

        if (methodAdvance != null){
            o = methodAdvance.createProxyObject(o);
        }

        singletonObjects.put(beanName, o);

        return o;
    }

    private Object createBeanInstance(Class<?> clazz) throws IllegalAccessException, InstantiationException{
        return clazz.newInstance();
    }

    private void createBean(String beanName) throws IllegalAccessException, InstantiationException {
        if (!singletonObjects.containsKey(beanName)) {
            Class<?> clazz = beanDefinitionMap.get(beanName).getClazz();

            Object o = createBeanInstance(clazz);
            Object proxy = initializeBean(beanName, o);
            populateBean(beanName, proxy, clazz);
        }
    }

    public <T> T getBean(Class<T> clazz){
        T bean = (T) singletonObjects.get(clazz.getName());
        if (bean != null){
            return bean;
        }

        // 多类型检查
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);
        for (Class<? extends T> subType : subTypes) {
            if (bean != null){
                log.error("无法注入，原因 -> 存在多个已注入实现");
            }
            bean = (T) singletonObjects.get(subType.getName());
        }

        return bean;
    }

    private Object populateBean(String beanName, Object o, Class<?> clazz){
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        Field[] fields = clazz.getDeclaredFields();

        try {
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowire.class)){
                    Class<?> fieldClass = field.getType();
                    String fieldBeanName = fieldClass.getName();

                    // 支持多态和指定指定实现注入
                    if (fieldClass.isInterface()){
                        Autowire autowire = field.getAnnotation(Autowire.class);
                        String value = autowire.value();
                        Set<Class<?>> subTypes = reflections.getSubTypesOf((Class)fieldClass);
                        if (!subTypes.isEmpty()){
                            if (StringUtils.isNotBlank(value)){
                                for (Class<?> subType : subTypes) {
                                    if (StringUtils.uncapitalize(subType.getName()).equals(StringUtils.uncapitalize(value))){
                                        fieldClass = subType;
                                    }
                                }
                            } else {
                                if (subTypes.size() > 1){
                                    log.error("无法注入，原因 -> 存在多个已注入实现");
                                } else {
                                    fieldClass = subTypes.iterator().next();
                                }
                            }
                        }
                    }

                    // 解决循环依赖
                    if (isCircular(fieldClass, clazz)){
                        if (earlySingletonObjects.containsKey(fieldBeanName)){
                            Object earlyObject = earlySingletonObjects.get(fieldBeanName);
                            field.setAccessible(true);
                            field.set(o, earlyObject);

                            earlySingletonObjects.remove(fieldBeanName);

                            continue;
                        } else {
                            earlySingletonObjects.put(beanName, o);
                        }
                    }


                    createBean(fieldClass.getName());

                    Object fieldObject = getBean(fieldClass);
                    field.setAccessible(true);
                    field.set(o, fieldObject);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return o;
    }

    public boolean isCircular(Class<?> fieldClass, Class<?> parentClass){
        Field[] fields = fieldClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowire.class) && field.getType().equals(parentClass)){
                return true;
            }
        }

        return false;
    }
}
