package ru.efive.dms.uifaces.beans.annotations;

/**
 * Created by EUpatov on 06.04.2017.
 */

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that spring bean instance will be in JSF like view scope.
 */
@Component
@Scope(value = "view", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Transactional
public @interface ViewScopedLazyDataModel {
    @AliasFor("name")
    String value() default "";
    @AliasFor(annotation = Component.class, attribute = "value")
    String name()  default "";
    @AliasFor(annotation = Transactional.class, attribute = "value")
    String transactionManager() default "";
}

