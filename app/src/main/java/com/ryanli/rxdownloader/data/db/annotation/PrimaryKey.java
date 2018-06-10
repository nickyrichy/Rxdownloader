package com.ryanli.rxdownloader.data.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Auther: RyanLi
 * Data: 2018-06-10 02:24
 * Description: 自定义 数据库 表主键注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {
    String value() default "";
}
