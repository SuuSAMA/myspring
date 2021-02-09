package com.leonardo.myspring.bean;

import com.leonardo.myspring.annotation.Component;

/**
 * Description:
 * Author: 邹良栋
 * CreateTime: 2021/2/5
 * Modifier: 邹良栋
 * UpdateTime: 2021/2/5
 */
@Component
public class Person {
    private String name = "Leonardo da Vinci";
    private Integer age = 502;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
