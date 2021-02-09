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


public class ApplicationContext {
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(16);
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(16);
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    private final Map<String, BeanMethodAdvance> aopMap = new ConcurrentHashMap<>(16);

    private final static String DEFAULT_ASPECT = "defaultAspect";

    private Logger log = LoggerFactory.getLogger(ApplicationContext.class);
    public void refresh(){
        try {
            // 注册bean的后置处理器用于提供切面
            registerBeanPostProcessors();
            // 注册所有的bean
            registerBeanDefinition();
            // 初始化所有的bean
            initSingleBean();
        } catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private void registerBeanPostProcessors() throws IllegalAccessException, InstantiationException {
        // 获取需要被扫描的路径下含有@Aspect注解的类
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(Aspect.class);

        for (Class<?> type : types) {
            // 获取pointCut
            PointCut pointCut = null;
            Method[] methods = type.getDeclaredMethods();
            for (Method method: methods){
                if (method.isAnnotationPresent(PointCut.class)){
                    pointCut = method.getAnnotation(PointCut.class);
                }
            }

            // 如果pointCut不为空，说明存在切入点
            if (pointCut != null){
                String beanName = pointCut.value();

                // 实例对应切面类
                BeanMethodAdvance beanMethodAdvance = (BeanMethodAdvance) type.newInstance();
                // 保存切面关系
                aopMap.put(beanName, beanMethodAdvance);
            }
        }

        // 注册一个默认的切面类
        Class<?> defaultAspect = reflections.getTypesAnnotatedWith(DefaultAspect.class).iterator().next();
        BeanMethodAdvance beanMethodAdvance = (BeanMethodAdvance) defaultAspect.newInstance();
        aopMap.put(DEFAULT_ASPECT, beanMethodAdvance);
    }

    private void registerBeanDefinition(){
        // 获取需要被扫描的路径下含有@Component注解的类
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);

        for (Class<?> clazz: classes){
            //包装bean描述
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setName(clazz.getName());
            beanDefinition.setClazz(clazz);
            beanDefinition.setClassPath(clazz.getPackage().getName());

            // 获取注解属性
            Component component = clazz.getAnnotation(Component.class);
            String key = clazz.getName();
            if (StringUtils.isNotBlank(component.value())){
                key = component.value();
            }

            // 注册bean
            beanDefinitionMap.put(key, beanDefinition);
        }
    }

    private void initSingleBean() throws InstantiationException, IllegalAccessException {
        // 获取所有被注册的bean
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            // 创建bean
            createBean(entry.getKey());
        }
    }

    private void createBean(String beanName) throws IllegalAccessException, InstantiationException {
        // 判断是否创建过
        if (!singletonObjects.containsKey(beanName)) {
            // 获取bean描述
            Class<?> clazz = beanDefinitionMap.get(beanName).getClazz();

            // 生成实例
            Object o = createBeanInstance(clazz);
            // 对实例生成代理对象，用以动态织入增强实现AOP
            Object proxy = initializeBean(beanName, o);
            // 对代理对象注入属性
            populateBean(beanName, proxy, clazz);
        }
    }

    private Object createBeanInstance(Class<?> clazz) throws IllegalAccessException, InstantiationException{
        // 创建类实例
        return clazz.newInstance();
    }

    private Object initializeBean(String beanName, Object o){
        BeanMethodAdvance methodAdvance = null;

        // 判断是否该对象是有自定义的切面，没有填充默认的
        if (aopMap.containsKey(beanName)){
            methodAdvance = aopMap.get(beanName);
        } else {
            methodAdvance = aopMap.get(DEFAULT_ASPECT);
        }

        // 创建代理对象
        if (methodAdvance != null){
            o = methodAdvance.createProxyObject(o);
        }

        // 向容器中存储代理对象
        singletonObjects.put(beanName, o);

        // 返回代理对象
        return o;
    }

    public <T> T getBean(Class<T> clazz){
        // 从容器中获取对象
        T bean = (T) singletonObjects.get(clazz.getName());
        // 如果获取到直接返回
        if (bean != null){
            return bean;
        }

        // 是否存在多个实现类，如果是则没法通过父类类型获取
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        // 如果只有一个实现类是允许通过父类类型获取
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(clazz);
        for (Class<? extends T> subType : subTypes) {
            if (bean != null){
                log.error("无法注入，原因 -> 存在多个已注入实现");
            }
            // 获取容器中的实现类
            bean = (T) singletonObjects.get(subType.getName());
        }

        return bean;
    }

    private Object populateBean(String beanName, Object o, Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(CustomConstant.basePackage);
        // 获取该类所有属性，暂时未实现通过set或构造器注入，但原理大同小异
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            // 如果存在@Autowire注解，说明需要填充
            if (field.isAnnotationPresent(Autowire.class)){
                Class<?> fieldClass = field.getType();
                String fieldBeanName = fieldClass.getName();

                // 如果为接口则获取实现类
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
                    // 判断二级缓存是否存在该对象，未使用三级缓存缘故目前该实例未存在复杂的继承结构，不需要工厂模式
                    if (earlySingletonObjects.containsKey(fieldBeanName)){
                        // 如果是循环依赖，且此时在A>B>A中A>B阶段时已经暴露过引用,所以这里能拿到前者引用
                        // 获取二级缓存中的对象，直接注入，然后删除二级缓存中
                        Object earlyObject = earlySingletonObjects.get(fieldBeanName);
                        field.setAccessible(true);
                        field.set(o, earlyObject);
                        earlySingletonObjects.remove(fieldBeanName);

                        continue;
                    } else {
                        // 否则在二级缓存中先提前暴露自己的引用
                        earlySingletonObjects.put(beanName, o);
                    }
                }

                // 先创建属性的实例，然后再回来。如果是循环依赖，此时在A>B>A中B>A，B已经创建过了，可以从下面直接获取到了
                createBean(fieldClass.getName());

                // 获取实例，然后注入
                Object fieldObject = getBean(fieldClass);
                field.setAccessible(true);
                field.set(o, fieldObject);
            }
        }
        return o;
    }


    public boolean isCircular(Class<?> fieldClass, Class<?> parentClass){
        // 获取所有属性字段
        Field[] fields = fieldClass.getDeclaredFields();

        // 如果属性在存在被引用的类，则说明产生循环引用，A引用B，而B又引用A
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowire.class) && field.getType().equals(parentClass)){
                return true;
            }
        }

        return false;
    }
}
