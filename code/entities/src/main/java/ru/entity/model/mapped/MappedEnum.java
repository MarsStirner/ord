package ru.entity.model.mapped;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * https://habrahabr.ru/post/77982/
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MappedEnum {
    String uniqueField() default "code";
    String value();
}
