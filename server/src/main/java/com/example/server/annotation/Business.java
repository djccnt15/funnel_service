package com.example.server.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


// this annotation is for annotating type of logic
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service  // register this class and implements as a spring bean
public @interface Business {
    
    @AliasFor(annotation = Service.class)
    String value() default "";
}
